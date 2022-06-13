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

import com.example.demo.generated.types.Show
import com.example.demo.services.ShowsService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument

import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Autowired

@DgsComponent
class ShowsDataFetcher(private val showsService: ShowsService) {

    @DgsQuery
    suspend fun shows(@InputArgument titleFilter : String?): List<Show> = coroutineScope {
        if(titleFilter != null) {
            showsService.shows().filter { it.title.contains(titleFilter) }
        } else {
            showsService.shows()
        }
    }
    /**
     * @InputArgument
     * : datafetcher 메서드에 인수로 입력 인수를 가져올 수 있다. 쿼리에서 보낸 유형과 Java/Kotlin의 유형을 일치시킨다.
     *   예를 들어, GraphQL의 Int는 Integer로, LocalDataTime은 java.util.ArrayList로 변경
     *
     *   Kotiln Data 클래스의 경우, 생성자의 모든 인수를 전달해야만 인스턴스를 생성할 수 있으므로
     *   Codegen plugin을 사용하면 제대로 작동함을 알 수 있다.
     *
     *   Kotlin의 경우, 입력 유형이 nullable인지 꼭 고려해야 한다.
     *   -> @InputArgument titleFilter : String? 처럼 뒤에 물음표를 붙여주는 이유다.
     */

    /**
     * @RequestHeader, @RequestParam, @CookieValue
     * HTTP 헤더 요청과 같은 다양한 요청에 맞추어 @InputArgument 대신 사용할 수 있다.
     * 추가적인 HTTP 요청 자체에 따른 활용 방법은 문서 확인할 것
     */
}