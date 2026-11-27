package o.lartifa.jam.onebot.cq

/**
 * Groovy / Java 友好的 CQ 组件类（与 [[Cq]] 内 case class 同串）。
 * Lambda 脚本用 `new ComponentAt(id)`，避免依赖 Scala 嵌套类字节码名。
 *
 * Author: sinar
 * 2026/11/27
 */
final class ComponentAt(id: Long) {
  override def toString: String = Cq.At(id).toCq
}

final class ComponentImage(fileOrUrl: String) {
  override def toString: String = Cq.Image(fileOrUrl).toCq
}

final class ComponentImageBase64(base64: String) {
  override def toString: String = Cq.ImageBase64(base64).toCq
}

final class ComponentFace(faceId: Int) {
  override def toString: String = Cq.Face(faceId).toCq
}

final class ComponentRecord(fileOrUrl: String) {
  override def toString: String = Cq.Record(fileOrUrl).toCq
}
