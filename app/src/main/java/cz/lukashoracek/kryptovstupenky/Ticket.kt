// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

const val TICKET_PREFIX = "KV_" // ISO-8859-1 charset characters only
const val TICKET_UID_LENGTH = 16 // 16 bytes = 128 bits
const val TICKET_SIGNATURE_LENGTH = 64 // 64 bytes for Ed25519 signature; Must match the signature length of the used algorithm!

const val TICKET_DATA_LENGTH = TICKET_PREFIX.length + TICKET_UID_LENGTH + TICKET_SIGNATURE_LENGTH

open class InvalidTicketDataException(message: String) : Exception(message)
class InvalidTicketDataPrefixException(message: String) : InvalidTicketDataException(message)

data class Ticket(
    val prefix: String, // constructor makes sure this matches TICKET_PREFIX
    val uid: ByteArray, // constructor makes sure uid is of length TICKET_UID_LENGTH
    val signature: ByteArray, // constructor makes sure signature is of length TICKET_SIGNATURE_LENGTH
) {
    init {
        if (prefix != TICKET_PREFIX) {
            throw InvalidTicketDataPrefixException("Invalid ticket prefix, got $prefix, expected $TICKET_PREFIX")
        }

        if (uid.size != TICKET_UID_LENGTH) {
            throw InvalidTicketDataException("Invalid ticket UID length, got ${uid.size} bytes, expected $TICKET_UID_LENGTH bytes")
        }

        if (signature.size != TICKET_SIGNATURE_LENGTH) {
            throw InvalidTicketDataException("Invalid ticket signature length, got ${signature.size} bytes, expected $TICKET_SIGNATURE_LENGTH bytes")
        }
    }

    companion object {
        fun encodeTicket(ticket: Ticket): ByteArray {
            return ticket.prefix.toByteArray(Charsets.ISO_8859_1) + ticket.uid + ticket.signature
        }

        fun decodeTicket(ticketData: ByteArray): Ticket {
            if (!ticketData.toString(Charsets.ISO_8859_1).startsWith(TICKET_PREFIX))
                throw InvalidTicketDataPrefixException("Invalid ticket prefix, expected $TICKET_PREFIX")

            if (ticketData.size != TICKET_DATA_LENGTH)
                throw InvalidTicketDataException("Ticket data does not match expected size! Got ${ticketData.size} bytes, expected $TICKET_DATA_LENGTH bytes")

            val ticket = Ticket(
                ticketData.sliceArray(TICKET_PREFIX.indices).toString(Charsets.ISO_8859_1),
                ticketData.sliceArray(TICKET_PREFIX.length..<TICKET_PREFIX.length + TICKET_UID_LENGTH),
                ticketData.sliceArray(TICKET_PREFIX.length + TICKET_UID_LENGTH..<TICKET_PREFIX.length + TICKET_UID_LENGTH + TICKET_SIGNATURE_LENGTH)
            )

            return ticket
        }
    }

    // Auto-generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Ticket

        if (prefix != other.prefix) return false
        if (!uid.contentEquals(other.uid)) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    // Auto-generated
    override fun hashCode(): Int {
        var result = prefix.hashCode()
        result = 31 * result + uid.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}