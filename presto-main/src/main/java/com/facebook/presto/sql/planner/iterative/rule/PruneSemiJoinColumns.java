/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.planner.iterative.rule;

import com.facebook.presto.Session;
import com.facebook.presto.matching.Pattern;
import com.facebook.presto.sql.planner.PlanNodeIdAllocator;
import com.facebook.presto.sql.planner.Symbol;
import com.facebook.presto.sql.planner.SymbolAllocator;
import com.facebook.presto.sql.planner.iterative.Lookup;
import com.facebook.presto.sql.planner.iterative.Rule;
import com.facebook.presto.sql.planner.plan.PlanNode;
import com.facebook.presto.sql.planner.plan.ProjectNode;
import com.facebook.presto.sql.planner.plan.SemiJoinNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.facebook.presto.sql.planner.iterative.rule.Util.pruneInputs;
import static com.facebook.presto.sql.planner.iterative.rule.Util.restrictOutputs;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class PruneSemiJoinColumns
        implements Rule
{
    private static final Pattern PATTERN = Pattern.typeOf(ProjectNode.class);

    @Override
    public Pattern getPattern()
    {
        return PATTERN;
    }

    @Override
    public Optional<PlanNode> apply(PlanNode node, Lookup lookup, PlanNodeIdAllocator idAllocator, SymbolAllocator symbolAllocator, Session session)
    {
        ProjectNode parent = (ProjectNode) node;

        PlanNode child = lookup.resolve(parent.getSource());
        if (!(child instanceof SemiJoinNode)) {
            return Optional.empty();
        }

        SemiJoinNode semiJoinNode = (SemiJoinNode) child;

        Optional<Set<Symbol>> prunedOutputs = pruneInputs(child.getOutputSymbols(), parent.getAssignments().getExpressions());
        if (!prunedOutputs.isPresent()) {
            return Optional.empty();
        }

        if (!prunedOutputs.get().contains(semiJoinNode.getSemiJoinOutput())) {
            return Optional.of(
                    parent.replaceChildren(ImmutableList.of(semiJoinNode.getSource())));
        }

        Set<Symbol> requiredSourceInputs = Streams.concat(
                prunedOutputs.get().stream()
                        .filter(symbol -> !symbol.equals(semiJoinNode.getSemiJoinOutput())),
                Stream.of(semiJoinNode.getSourceJoinSymbol()),
                semiJoinNode.getSourceHashSymbol().map(Stream::of).orElse(Stream.empty()))
                .collect(toImmutableSet());

        return restrictOutputs(idAllocator, semiJoinNode.getSource(), requiredSourceInputs)
                .map(newSource ->
                        parent.replaceChildren(ImmutableList.of(
                                semiJoinNode.replaceChildren(ImmutableList.of(
                                        newSource, semiJoinNode.getFilteringSource())))));
    }
}
