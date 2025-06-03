// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import org.bouncycastle.jcajce.provider.asymmetric.edec.BCEdDSAPrivateKey
import org.bouncycastle.jcajce.spec.EdDSAParameterSpec
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.nio.ByteBuffer
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

class Crypto {
    companion object {
        private val secureRandom: SecureRandom = SecureRandom()

        fun setupBouncyCastle() {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME) // Android seems to include Bouncy Castle by default, remove it
            Security.insertProviderAt(BouncyCastleProvider(), 1) // Insert our own Bouncy Castle
        }

        fun decodePublicKeyFromBase64(publicKeyBase64: String): PublicKey {
            val publicKeySpec = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64))
            val keyFactory = KeyFactory.getInstance("Ed25519", BouncyCastleProvider.PROVIDER_NAME)
            val publicKey = keyFactory.generatePublic(publicKeySpec)

            return publicKey
        }

        fun decodePrivateKeyFromBase64(privateKeyBase64: String): PrivateKey {
            val privateKeySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64))
            val keyFactory = KeyFactory.getInstance("Ed25519", BouncyCastleProvider.PROVIDER_NAME)
            val privateKey = keyFactory.generatePrivate(privateKeySpec)

            return privateKey
        }

        fun encodePublicKeyToBase64(publicKey: PublicKey): String {
            return Base64.getEncoder().encodeToString(publicKey.encoded)
        }

        fun encodePrivateKeyToBase64(privateKey: PrivateKey): String {
            return Base64.getEncoder().encodeToString(privateKey.encoded)
        }

        fun privateKeyToPublicKey(privateKey: PrivateKey): PublicKey {
            val ed25519PrivateKey = privateKey as BCEdDSAPrivateKey
            return ed25519PrivateKey.publicKey as PublicKey
        }

        fun generateNewKeyPair(): KeyPair {
            val keyPairGenerator = KeyPairGenerator.getInstance("Ed25519", BouncyCastleProvider.PROVIDER_NAME)
            keyPairGenerator.initialize(EdDSAParameterSpec("Ed25519"))
            return keyPairGenerator.generateKeyPair()
        }

        fun verifyTicket(ticket: Ticket, publicKey: PublicKey): Boolean {
            val signature = Signature.getInstance("Ed25519")
            signature.initVerify(publicKey)

            signature.update(ticket.uid)
            return signature.verify(ticket.signature)
        }

        fun generateTicket(privateKey: PrivateKey): Ticket {
            val signature = Signature.getInstance("Ed25519", BouncyCastleProvider.PROVIDER_NAME)
            signature.initSign(privateKey)

            // Generate ticket UID from cryptographically random data
            val ticketUID = ByteBuffer.allocate(TICKET_UID_LENGTH)
            secureRandom.nextBytes(ticketUID.array())

            // Sign data
            ticketUID.rewind() // This is not needed but let's make sure the position is 0 before calling signature.update
            signature.update(ticketUID)
            val signatureData: ByteArray = signature.sign()

            return Ticket(TICKET_PREFIX, ticketUID.array(), signatureData)
        }
    }
}