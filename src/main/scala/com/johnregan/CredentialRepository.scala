package com.johnregan

import com.yubico.webauthn.data.{ByteArray, PublicKeyCredentialDescriptor}
import com.yubico.webauthn.{CredentialRepository, RegisteredCredential}

import java.util
import java.util.Optional
import scala.jdk.CollectionConverters.SetHasAsJava

class MockCredentialRepository(credentialId: ByteArray) extends CredentialRepository {

  override def getCredentialIdsForUsername(
      username: String
  ): util.Set[PublicKeyCredentialDescriptor] =
    Set(
      PublicKeyCredentialDescriptor
        .builder()
        .id(credentialId)
        .build()
    ).asJava

  override def getUserHandleForUsername(username: String): Optional[ByteArray] = ???

  override def getUsernameForUserHandle(userHandle: ByteArray): Optional[String] = ???

  override def lookup(
      credentialId: ByteArray,
      userHandle: ByteArray
  ): Optional[RegisteredCredential] = ???

  override def lookupAll(credentialId: ByteArray): util.Set[RegisteredCredential] = ???
}
