package webapp

package object workshop {
  implicit class TODO(val any: Any) extends AnyVal {
    def TODO = throw new NotImplementedError(any.toString)
  }
}
