package vkminer.serialize

import vkminer.dom.VkEnvironment

trait SerializerComponent {this: VkEnvironment =>

  val workingDirectory: String

  trait Serializer[T] {
    val extension: String
    def file(name: String) = new java.io.File(workingDirectory, s"$name.$extension")

    def notImplemented = throw new UnsupportedOperationException

    def serialize  (target: T, name: String): Unit = notImplemented
    def deserialize(name: String           ): T    = notImplemented
  }
}