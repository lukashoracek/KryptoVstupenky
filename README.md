# KryptoVstupenky
An Android application allowing **offline generation and validation of QR code tickets** using asymmetric cryptography, specifically using the digital signature algorithm Ed25519.  
This project was created as part of a graduation thesis / a high school final project.

## Important limitation
**If multiple devices are used to verify tickets it's not possible to prevent repeated use of the same ticket.** However, if only a single device is used for verifying tickets, repeated use of the same ticket is prevented thanks to a local database of scanned tickets and such ticket will not be considered valid.

## Used external libraries
- Bouncy Castle - Ed25519 implementation
- Google ML Kit - scanning QR codes
- zxing - generating QR codes
- CameraX - camera usage
- DataStore - persistent data storage
- Material Design Icons - icons