package webapp

package object workshop {
  implicit class AnyExtensionMethods(val any: Any) extends AnyVal {
    def TODO = throw new NotImplementedError(any.toString)
  }

  type TODO = Nothing

  def TODO: Nothing = ???
}
