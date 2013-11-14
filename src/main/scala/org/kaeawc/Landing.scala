package org.kaeawc



import spray.http._
import StatusCodes._

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.routing.authentication._
import scala.concurrent.{Future,ExecutionContext}
import ExecutionContext.Implicits.global

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class LandingActor extends Actor with Landing {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait Landing extends HttpService {

  type UserPassAuthenticator[T] = Option[UserPass] ⇒ Future[Option[T]]

  def myUserPassAuthenticator(userPass: Option[UserPass]): Future[Option[String]] =
  Future {
    if (userPass.exists(up => up.user == "John" && up.pass == "p4ssw0rd")) Some("John")
    else None
  }

  val myRoute =
      path("secured") {
        authenticate(BasicAuth(myUserPassAuthenticator _, realm = "secure site")) { userName =>
          complete(s"The user is '$userName'")
        }
      }

}