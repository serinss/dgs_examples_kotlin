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

package com.example.demo.datafetchers

import com.example.demo.dataloaders.ReviewsDataLoader
import com.example.demo.generated.DgsConstants
import com.example.demo.generated.types.Review
import com.example.demo.generated.types.Show
import com.example.demo.generated.types.SubmittedReview
import com.example.demo.services.ReviewsService
import com.netflix.graphql.dgs.*
import org.dataloader.DataLoader
import org.reactivestreams.Publisher
import java.util.concurrent.CompletableFuture

@DgsComponent
class ReviewsDataFetcher(private val reviewsService: ReviewsService) {

    /**
     * 이 datafetcher는 Show의 reviews들을 resolve하기 위해 호출될 것
     * 각 Show에 대해 호출되므로 Show를 10번 로드하면 이 메서드가 10번 호출된다.
     * N+1 문제를 피하기 위해 DataLoader를 사용
     * DataLoader는 각각의 쇼 ID에 대해 호출되지만,
     * 실제 로딩은 ReviewsDataLoader의 "load" 메서드에 대한 단일 메서드 호출로 일괄 처리된다.
     * 이 작업이 올바르게 작동하려면, 데이터 수집기에서 CompletableFuture를 반환해야 한다.
     */
    @DgsData(parentType = DgsConstants.SHOW.TYPE_NAME, field = DgsConstants.SHOW.Reviews)
    fun reviews(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<Review>> {
        // 이름으로 DataLoader를 로드하는 대신
        // DgsDataFetchingEnvironment(=dfe)를 사용하여 DataLoader 클래스 이름을 검색할 수 있다.
        // 해당 이름은 문자열로 전달
        val reviewsDataLoader: DataLoader<Int, List<Review>> = dfe.getDataLoader(ReviewsDataLoader::class.java)

        // reviews 필드가 Show에 있으므로, getSource() 메서드는 Show 인스턴스를 반환합니다.
        val show : Show = dfe.getSource()

        // DataLoader에서 reviews를 로드합니다. 이 호출은 비동기적이며 DataLoader 메커니즘에 의해 일괄 처리됩니다.
        return reviewsDataLoader.load(show.id)
    }

    /**
     * @DgsData(paraentType = 필드를 포함하는 유형, field = datafetcher가 담당하는 필드)
     * 필드 : build로 생성된 POJO와 연결되어 있음
     * 필드의 매개변수가 설정되지 않은 경우, 메서드의 이름이 필드 이름으로 사용된다.
     *
     * 만약, Show 전체를 가져오는 것이 아니라, reviews만을 가져오고 싶다면, 별도의 Datafetcher를 만드는 것이 좋다.
     * 하지만 이 때에 N+1 문제가 발생할 수 있으므로 DataLoader를 사용해야 함을 주의!
     */

    /**
     * 여러 개의 @DgsData를 관리하기 위해서 @DgsData.List 활용 가능
     * @DgsData.List(
        DgsData(parentType = "Query", field = "movies"),
        DgsData(parentType = "Query", field = "shows")
        )
     * 단, @DgsQuery, @DgsMutation은 단일 메서드에서 여러 번 정의할 수 없다.
     */


    @DgsMutation
    fun addReview(@InputArgument review: SubmittedReview): List<Review> {
        //@InputArgument("input")을 활용하여 더 쉽게 입력할 수 있음

        reviewsService.saveReview(review) //리뷰 삽입
        return reviewsService.reviewsForShow(review.showId)?: emptyList()
    }

    /**
     * @DgsMutation
     * : 데이터를 수정할 경우에 사용, 명시적으로 뮤테이션를 통해 전송되어야 한다는 규칙을 정하는 것이 좋다.
     * ex. createReview, addReview 등등의 CRUD작업
     *
     * 뮤테이션은 다중 필드를 포함할 수 있다. 쿼리와의 중요한 차이점은
     * ** 쿼리 필드는 병렬로 실행되지만 뮤테이션 필드는 하나씩 차례대로 실행된다.
     */

    @DgsSubscription
    fun reviewAdded(@InputArgument showId: Int): Publisher<Review> {
        return reviewsService.getReviewsPublisher()
    }
}