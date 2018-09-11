/*
 * Copyright 2018 HM Revenue & Customs
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
 */

package uk.gov.hmrc.ia.service

import org.scalatest.mockito.MockitoSugar
import reactivemongo.api.commands.MultiBulkWriteResult
import uk.gov.hmrc.ia.repository.ValidUtrRepo
import uk.gov.hmrc.ia.support.Spec
import uk.gov.hmrc.ia.support.TestData.validUtrs
import org.mockito.Mockito.when
import uk.gov.hmrc.ia.domain.GreenUtr

import scala.concurrent.Future

class GreenUtrServiceSpec extends Spec  with MockitoSugar{
  class BulkInsertRejected extends Exception("No objects inserted. Error converting some or all to JSON")
  val mockValidRepo = mock[ValidUtrRepo]
  val greenUtrService = new GreenUtrService(mockValidRepo)
  val writeResult = MultiBulkWriteResult()

  "The  GreenUtrService should insert Utrs" in {
    when(mockValidRepo.bulkInsert(validUtrs)).thenReturn(Future.successful(writeResult))
    val result: Unit =  greenUtrService.bulkInsert(validUtrs).futureValue
    result shouldBe ()
  }

  "Return an exception if it fails to insert " in {
    when(mockValidRepo.bulkInsert(validUtrs)).thenReturn(Future.failed[MultiBulkWriteResult](new BulkInsertRejected()))
    intercept[RuntimeException] {
      greenUtrService.bulkInsert(validUtrs).futureValue
    }.getMessage contains "No objects inserted. Error converting some or all to JSON" shouldBe true
  }

  "return true if the item is in the db in" in {
    when(mockValidRepo.find("utr" -> "1234567890")).thenReturn(Future.successful(List(GreenUtr("1234567890"))))
    val result =  greenUtrService.isGreenUtr("1234567890").futureValue
    result shouldBe true
  }
  "return false if the item is in the db in" in {
    when(mockValidRepo.find("utr" -> "1234567890")).thenReturn(Future.successful(List()))
    val result =  greenUtrService.isGreenUtr("1234567890").futureValue
    result shouldBe false
  }
}