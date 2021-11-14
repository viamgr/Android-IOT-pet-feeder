package com.viam.feeder.domain

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.repositories.socket.DeviceRepository
import com.viam.feeder.domain.usecase.ConnectionStatus
import com.viam.feeder.domain.usecase.hasPingFromIp
import com.viam.feeder.shared.API_IP
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_IP
import com.viam.feeder.shared.DeviceConnectionTimoutException
import com.viam.feeder.shared.NetworkNotAvailableException
import com.viam.resource.Resource
import com.viam.resource.dataOrNull
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import io.mockk.verify
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Rule
    @JvmField
    val coroutineRule = CoroutineRule()

    @RelaxedMockK
    lateinit var coroutinesDispatcherProvider: CoroutinesDispatcherProvider

    @RelaxedMockK
    lateinit var deviceRepository: DeviceRepository


    lateinit var usecase: ConnectionStatus

    private val coroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { coroutinesDispatcherProvider.io } returns coroutineDispatcher
        usecase = ConnectionStatus(coroutinesDispatcherProvider, deviceRepository)
    }

    @Test
    fun addition_isCorrect0() = runBlocking {
        mockkStatic("com.viam.feeder.domain.usecase.PingUtils")

        every {
            hasPingFromIp(DEFAULT_ACCESS_POINT_IP, any())
        } returns false

        val result = usecase(
            ConnectionStatus.NetworkOptions(
                isAvailable = true,
                isWifi = true,
                wifiName = "test",
                localIp = it.localIp
            )
        )
            .flowOn(coroutineDispatcher)
            .single()
        println(result)
        assertEquals(result.dataOrNull()?.host, API_IP)
    }

    @Test
    fun addition_isCorrect() = runBlocking {
        mockkStatic("com.viam.feeder.domain.usecase.PingUtils")

        every {
            hasPingFromIp(any(), any())
        } returns true

        val result = usecase(
            ConnectionStatus.NetworkOptions(
                isAvailable = true,
                isWifi = true,
                wifiName = "test",
                localIp = it.localIp
            )
        )
            .flowOn(coroutineDispatcher)
            .single()
        assertThat(result, instanceOf(Resource.Success::class.java))
    }

    @Test
    fun addition_isCorrect2() = runBlocking {
        mockkStatic("com.viam.feeder.domain.usecase.PingUtils")

        every {
            hasPingFromIp(any(), any())
        } returns false

        val result = usecase(
            ConnectionStatus.NetworkOptions(
                isAvailable = true,
                isWifi = true,
                wifiName = "test",
                localIp = it.localIp
            )
        )
            .flowOn(coroutineDispatcher)
            .single()
        assertThat(result, instanceOf(Resource.Error::class.java))
    }

    @Test
    fun addition_isCorrect3() = runBlocking {
        mockkStatic("com.viam.feeder.domain.usecase.PingUtils")

        every {
            hasPingFromIp(any(), any())
        } returns false

        val result = usecase(
            ConnectionStatus.NetworkOptions(
                isAvailable = false,
                isWifi = false,
                wifiName = "test",
                localIp = it.localIp
            )
        )
            .flowOn(coroutineDispatcher)
            .single()
        assertThat(result, instanceOf(Resource.Error::class.java))
        assertThat(
            (result as Resource.Error).exception,
            instanceOf(NetworkNotAvailableException::class.java)
        )
    }

    @Test
    fun addition_isCorrect4() = runBlocking {
        mockkStatic("com.viam.feeder.domain.usecase.PingUtils")

        every {
            hasPingFromIp(any(), any())
        } returns false

        val result = usecase(
            ConnectionStatus.NetworkOptions(
                isAvailable = true,
                isWifi = true,
                wifiName = "test",
                localIp = it.localIp
            )
        )
            .flowOn(coroutineDispatcher)
            .single()
        assertThat(result, instanceOf(Resource.Error::class.java))
        assertThat(
            (result as Resource.Error).exception,
            instanceOf(NetworkNotAvailableException::class.java)
        )
    }

    @Test
    fun addition_isCorrect5() = runBlocking {
        mockkStatic("com.viam.feeder.domain.usecase.PingUtils")

        every {
            hasPingFromIp(DEFAULT_ACCESS_POINT_IP, any())
        } returns false
        every {
            hasPingFromIp(API_IP, any())
        } returns false

        val result = usecase(
            ConnectionStatus.NetworkOptions(
                isAvailable = true,
                isWifi = true,
                wifiName = "test",
                localIp = it.localIp
            )
        )
            .flowOn(coroutineDispatcher)
            .single()
        verify(exactly = 2) { hasPingFromIp(any(), any()) }
        println(result)

        assertThat(result, instanceOf(Resource.Error::class.java))
    }

    @Test
    fun addition_isCorrect6() = runBlocking {
        mockkStatic("com.viam.feeder.domain.usecase.PingUtils")

        every {
            Thread.sleep(6000)
            hasPingFromIp(any(), any())
        } returns false

        val result = usecase(
            ConnectionStatus.NetworkOptions(
                isAvailable = true,
                isWifi = true,
                wifiName = "test",
                localIp = it.localIp
            )
        )
            .flowOn(coroutineDispatcher)
            .single()

        assertThat(
            (result as Resource.Error).exception,
            instanceOf(DeviceConnectionTimoutException::class.java)
        )
    }

    @Test
    fun addition_isCorrect7() = runBlocking {
        mockkStatic("com.viam.feeder.domain.usecase.PingUtils")

        every {
            Thread.sleep(2000)
            hasPingFromIp(DEFAULT_ACCESS_POINT_IP, any())
        } returns true
        every {
            Thread.sleep(4000)
            hasPingFromIp(API_IP, any())
        } returns true

        val result = usecase(
            ConnectionStatus.NetworkOptions(
                isAvailable = true,
                isWifi = true,
                wifiName = "test",
                localIp = it.localIp
            )
        )
            .flowOn(coroutineDispatcher)
            .single()
        println(result)

        assertEquals(result.dataOrNull()?.host, DEFAULT_ACCESS_POINT_IP)
    }
}