package tasks

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.CollectingOutputReceiver
import com.android.ddmlib.IDevice
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.mock
import internal.DeviceCommunicator
import internal.OutputReceiverProvider
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import unitTest.tasks.internal.BaseUnitTest
import tasks.internal.DefaultPluginTask
import java.io.File

class CheckWifiTaskUnitTest : BaseUnitTest() {

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    val device: IDevice = mock()
    val bridge: AndroidDebugBridge = mock()
    val outputReceiver: CollectingOutputReceiver = mock()
    val outputReceiverProvider: OutputReceiverProvider = mock()
    val defaultPluginTask: DefaultPluginTask = mock()

    val deviceCommunicator = DeviceCommunicator(bridge, outputReceiverProvider)
    val noDevices = emptyArray<IDevice>()
    val wifi = "wifi"
    val devices = arrayOf(device)
    val mNetworkInfo = "mNetworkInfo [type: WIFI[], extra: \"$wifi\"]"

    lateinit var projectDir: File
    lateinit var project: Project
    lateinit var task: CheckWifiTask

    @Before
    fun setup() {
        projectDir = temporaryFolder.root
        projectDir.mkdirs()

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        task = project.tasks.create("CheckWifiTask", CheckWifiTask::class.java)

        task.communicator = deviceCommunicator
        task.wifi = wifi

        given(outputReceiverProvider.get()).willReturn(outputReceiver)
    }

    @Test(expected = GradleException::class)
    fun `throw gradle exception when string in extension is empty`() {
        task.wifi = ""
        given(bridge.devices).willReturn(devices)

        task.runTask1()
    }

    @Test(expected = GradleException::class)
    fun `throw gradle exception when string in extension is blank`() {
        task.wifi = " "
        given(bridge.devices).willReturn(devices)

        task.runTask1()
    }

    @Test
    fun `no gradle exception is thrown when connected to right wifi`() {
        given(bridge.devices).willReturn(devices)
        given(outputReceiver.output).willReturn(mNetworkInfo)

        task.runTask2(device)

        thenDeviceShouldGetDetails(device)
    }
}