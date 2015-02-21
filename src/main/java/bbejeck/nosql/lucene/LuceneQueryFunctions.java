
/*
 * *
 *
 *
 * Copyright 2015 Bill Bejeck
 *
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
 *
 *
 */

package bbejeck.nosql.lucene;

import bbejeck.nosql.util.Collectors;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.queries.FilterClause;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * User: Bill Bejeck
 * Date: 8/12/14
 * Time: 11:25 PM
 */
public interface LuceneQueryFunctions {


    BiFunction<String, String, Term> createTerm = Term::new;

    BiFunction<String, String, Function<QueryType, Function<BooleanClause.Occur, BooleanClause>>> toBooleanClause = (f, t) -> qt -> bco -> new BooleanClause(qt.query(f, t), bco);

    BiFunction<Term, Class<? extends Query>, Query> toQuery = (t, c) -> {
        try {
            return c.getConstructor(Term.class).newInstance(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };


    Function<String, Function<String, Term>> termFunction = field -> text -> createTerm.apply(field, text);
    Function<String, Stream<String>> toWordStream = s -> Lists.newArrayList(Splitter.on(" ").split(s)).stream();
    BiFunction<String, String, PhraseQuery> toPhraseQuery = (field, text) ->
            toWordStream.apply(text).map(termFunction.apply(field)).collect(Collectors.of(PhraseQuery::new, PhraseQuery::add));

    Function<List<BooleanClause>, BooleanQuery> toBooleanQuery = l -> l.stream().collect(Collectors.of(BooleanQuery::new, BooleanQuery::add));
    Function<List<FilterClause>,BooleanFilter> toBooleanFilter = filters -> filters.stream().collect(Collectors.of(BooleanFilter::new,BooleanFilter::add));

}