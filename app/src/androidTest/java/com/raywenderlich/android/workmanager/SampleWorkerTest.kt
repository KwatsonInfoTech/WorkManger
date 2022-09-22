/*
 *
 *  * Copyright (c) 2020 Razeware LLC
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 *  * distribute, sublicense, create a derivative work, and/or sell copies of the
 *  * Software in any work that is designed, intended, or marketed for pedagogical or
 *  * instructional purposes related to programming, coding, application development,
 *  * or information technology.  Permission for such use, copying, modification,
 *  * merger, publication, distribution, sublicensing, creation of derivative works,
 *  * or sale is expressly withheld.
 *  *
 *  * This project and source code may use libraries or frameworks that are
 *  * released under various Open-Source licenses. Use of those libraries and
 *  * frameworks are governed by their own individual licenses.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 */

package com.raywenderlich.android.workmanager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.*
import androidx.work.testing.WorkManagerTestInitHelper
import com.raywenderlich.android.workmanager.workers.SampleWorker
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class SampleWorkerTest {
  @get:Rule
  var instantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  var workerManagerTestRule = WorkManagerTestRule()


  @Test
  fun testWorkerInitialDelay() {
    val inputData = workDataOf("Worker" to "sampleWorker")

    // Create Work request.
    val request = OneTimeWorkRequestBuilder<SampleWorker>()
      .setInitialDelay(10, TimeUnit.SECONDS)
      .setInputData(inputData)
      .build()


    val testDriver = WorkManagerTestInitHelper.getTestDriver(workerManagerTestRule.targetContext)
    val workManager = workerManagerTestRule.workManager

    // Enqueue the request
    workManager.enqueue(request).result.get()

    // Set Initial Delay
    testDriver?.setInitialDelayMet(request.id)

    // Get WorkInfo and outputData
    val workInfo = workManager.getWorkInfoById(request.id).get()

    // Assert
    assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
  }

  @Test
  fun testPeriodicSampleWorker() {
    val inputData = workDataOf("Worker" to "sampleWorker")

    // Create Work request.
    val request = PeriodicWorkRequestBuilder<SampleWorker>(15, TimeUnit.MINUTES)
      .setInputData(inputData)
      .build()

    val testDriver = WorkManagerTestInitHelper.getTestDriver(workerManagerTestRule.targetContext)
    val workManager = workerManagerTestRule.workManager

    // Enqueue the request
    workManager.enqueue(request).result.get()

    // Complete period delay
    testDriver?.setPeriodDelayMet(request.id)

    // Get WorkInfo and outputData
    val workInfo = workManager.getWorkInfoById(request.id).get()
    assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
  }

  @Test
  fun testAllConstraintsAreMet() {
    val inputData = workDataOf("Worker" to "sampleWorker")

    // Create Constraints.
    val constraints = Constraints.Builder()
      // Add network constraint.
      .setRequiredNetworkType(NetworkType.CONNECTED)
      // Add battery constraint.
      .setRequiresBatteryNotLow(true)
      .build()

    // Create Work request.
    val request = OneTimeWorkRequestBuilder<SampleWorker>()
      // Add constraints
      .setConstraints(constraints)
      .setInputData(inputData)
      .build()

    val workManager = WorkManager.getInstance(workerManagerTestRule.targetContext)

    // Enqueue the request
    workManager.enqueue(request).result.get()

    // Simulate constraints
    WorkManagerTestInitHelper.getTestDriver(workerManagerTestRule.targetContext)
      ?.setAllConstraintsMet(request.id)

    val workInfo = workManager.getWorkInfoById(request.id).get()

    // Assert
    assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
  }
}