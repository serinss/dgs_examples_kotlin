/*
 * Copyright 2021 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.demo.dataloaders

import com.example.demo.generated.types.Review
import com.example.demo.services.ReviewsService
import com.netflix.graphql.dgs.DgsDataLoader
import org.dataloader.MappedBatchLoader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlin.streams.toList

@DgsDataLoader(name = "reviews")
class ReviewsDataLoader(val reviewsService: ReviewsService): MappedBatchLoader<Int, List<Review>> {
    /**
     * DataLoader -> sub-Query를 호출 할 때 생기는 N+1 문제를 해결(=batch, 한번에 모아서 일괄 처리)
     * 이 메서드는 여러 datafetcher가 DataLoader에서 load() 메서드를 사용하는 경우에도 한 번 호출됩니다.
     * 이렇게 하면 개별 Show가 아닌 한 번의 통화로 모든 Show에 대한 리뷰를 로드할 수 있습니다.
     *
     * MappedBatchLoader : BatchLoader의 키와 유형에 대한 매개변수화
     */
    override fun load(keys: MutableSet<Int>): CompletionStage<Map<Int, List<Review>>> {
        return CompletableFuture.supplyAsync { reviewsService.reviewsForShows(keys.stream().toList()) }
    }
    /**
     * CompletableFuture : 비동기 요청 처리
     * supplyAsync() : 파라미터로 Supplier 인터페이스를 받음. 비동기 상황에서의 작업을 콜백 함수로 넘기고,
     *                 작업 수행 여부와 관계 없이 CompletableFuture 객체로 다음 로직을 이어나갈 수 있음
     */
}