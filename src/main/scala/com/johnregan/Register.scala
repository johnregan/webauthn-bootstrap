package com.johnregan

import com.yubico.webauthn.{FinishRegistrationOptions, RelyingParty, StartRegistrationOptions}
import com.yubico.webauthn.data._

import java.security.SecureRandom
import scala.jdk.CollectionConverters.SetHasAsJava

case class RegistrationRequest(
    username: String,
    credentialNickname: Option[String],
    requestId: ByteArray,
    publicKeyCredentialCreationOptions: PublicKeyCredentialCreationOptions,
    sessionToken: Option[ByteArray]
)

object RegistrationRequest {
  def apply(
      username: String,
      credentialNickname: String,
      publicKeyCredentialCreationOptions: PublicKeyCredentialCreationOptions
  ): RegistrationRequest =
    RegistrationRequest(
      username = username,
      credentialNickname = Some(credentialNickname),
      requestId = Register.generateRandom(32),
      publicKeyCredentialCreationOptions = publicKeyCredentialCreationOptions,
      sessionToken = None
    )

}

object Register {

  private val mockCredentialId = generateRandom(32)
  private val mockRepository   = new MockCredentialRepository(mockCredentialId)

  private val rpIdentity = RelyingPartyIdentity
    .builder()
    .id("appPropertiesgetRelyingPartyId()")
    .name("appProperties.getRelyingPartyName()")
    .build()

  private val rp = RelyingParty
    .builder()
    .identity(rpIdentity)
    .credentialRepository(mockRepository)
    .origins(Set("appProperties.getRelyingPartyOrigins()").asJava)
    .build()

  def startRegistration(
      userIdentity: UserIdentity,
      residentKeyRequirement: ResidentKeyRequirement
  ): PublicKeyCredentialCreationOptions =
    rp.startRegistration(
      StartRegistrationOptions
        .builder()
        .user(userIdentity)
        .authenticatorSelection(
          AuthenticatorSelectionCriteria
            .builder()
            .residentKey(residentKeyRequirement)
            .build()
        )
        .build()
    )

  def finishRegistration(
      request: RegistrationRequest,
      credential: PublicKeyCredential[
        AuthenticatorAttestationResponse,
        ClientRegistrationExtensionOutputs
      ]
  ) =
    rp.finishRegistration(
      FinishRegistrationOptions
        .builder()
        .request(request.publicKeyCredentialCreationOptions)
        .response(credential)
        .build()
    )

  def getUser(username: String, displayName: String): UserIdentity =
    UserIdentity
      .builder()
      .name(username)
      .displayName(displayName)
      .id(generateRandom(32))
      .build()

  def generateRandom(length: Int): ByteArray = {
    val bytes  = new Array[Byte](length)
    val random = new SecureRandom()
    random.nextBytes(bytes)
    new ByteArray(bytes)
  }
}

object Runner {

  val username               = "jr@sirius.com"
  val displayName            = "jr"
  val residentKeyRequirement = ResidentKeyRequirement.REQUIRED

  val user: UserIdentity = Register.getUser(username, displayName)

  val publicKeyCredentialCreationOptions: PublicKeyCredentialCreationOptions =
    Register.startRegistration(user, residentKeyRequirement)

  val registrationRequest = RegistrationRequest(
    username,
    displayName,
    publicKeyCredentialCreationOptions
  )

//  Register.finishRegistration(registrationRequest, )

}
