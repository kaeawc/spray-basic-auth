package org.kaeawc

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class LandingSpec extends Specification with Specs2RouteTest with Landing {
  def actorRefFactory = system
  
  "MyService" should {

    "return a greeting for GET requests to the root path" in {
      Get("/secured") ~> sealRoute(myRoute) ~> check {
        status === StatusCodes.Unauthorized
        responseAs[String] === "The resource requires authentication, which was not supplied with the request"
        header[HttpHeaders.`WWW-Authenticate`].get.challenges.head === HttpChallenge("Basic", "secure site")
      }

      val validCredentials = BasicHttpCredentials("John", "p4ssw0rd")
      Get("/secured") ~>
        addCredentials(validCredentials) ~> // adds Authorization header
        myRoute ~> check {

        val response = responseAs[String]
        println ("the response was: " + response)
        response === "The user is 'John'"
      }

      val invalidCredentials = BasicHttpCredentials("Peter", "pan")
      Get("/secured") ~>
        addCredentials(invalidCredentials) ~>  // adds Authorization header
        sealRoute(myRoute) ~> check {
          status === StatusCodes.Unauthorized
          responseAs[String] === "The supplied authentication is invalid"
          header[HttpHeaders.`WWW-Authenticate`].get.challenges.head === HttpChallenge("Basic", "secure site")
        }
    }
  }
}
