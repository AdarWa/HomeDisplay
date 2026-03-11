package net.adarw.serialization

import java.nio.file.Paths
import kotlin.test.Test
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.schema.ProtoBufSchemaGenerator
import net.adarw.rpc.definitions.messages.RegisterMessage

class ProtoBufSerTest {
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `test kotlin dataclass to protobuf`() {
        println(Paths.get("").toAbsolutePath().toString())
        println(
            ProtoBufSchemaGenerator.generateSchemaText(
                RegisterMessage.serializer().descriptor
            )
        )
    }
}
