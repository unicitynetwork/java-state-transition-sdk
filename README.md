# Unicity Java State Transition SDK

A Java SDK for interacting with the Unicity network, enabling state transitions and token operations
with cross-platform support for JVM and Android 12+.

## Features

- **Token Operations**: Mint, transfer, and manage fungible tokens
- **State Transitions**: Submit and verify state transitions on the Unicity network
- **Cross-Platform**: Works on standard JVM and Android 12+ (API level 31+)
- **Type-Safe**: Strongly typed API with comprehensive error handling
- **CBOR Support**: Built-in CBOR encoding/decoding using Jackson
- **Async Operations**: All network operations return `CompletableFuture` for non-blocking execution

## Requirements

- Java 11 or higher
- Android 12+ (API level 31+) for Android platform
- Gradle 8.8 or higher

## Installation

### Using JitPack

Add JitPack repository:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

#### For Android Projects:

```groovy
dependencies {
    implementation 'com.github.unicitynetwork:java-state-transition-sdk:1.1:android'
}
```

#### For JVM Projects:

```groovy
dependencies {
    implementation 'com.github.unicitynetwork:java-state-transition-sdk:1.1:jvm'
}
```

### Using Local Maven

```groovy
dependencies {
    implementation 'org.unicitylabs:java-state-transition-sdk:1.1-SNAPSHOT'
}
```

## Quick Start

### Util methods

```java
private static final SecureRandom RANDOM = new SecureRandom();

/**
 * Generate random bytes of specified length.
 */
public static byte[] randomBytes(int length) {
  byte[] bytes = new byte[length];
  RANDOM.nextBytes(bytes);
  return bytes;
}
```

### Initialize the Client

```java
// Connect to the Unicity test network
String aggregatorUrl = "https://gateway-test.unicity.network";
DefaultAggregatorClient aggregatorClient = new DefaultAggregatorClient(aggregatorUrl);
StateTransitionClient client = new StateTransitionClient(aggregatorClient);

// Create root trust base from classpath
RootTrustBase trustbase = RootTrustBase.fromJson(
    new String(getClass().getResourceAsStream("/trust-base.json").readAllBytes())
);
```

### Mint a Token

```java
byte[] secret = "minter_secret".getBytes(StandardCharsets.UTF_8);
// Generate data for token
TokenId tokenId = new TokenId(randomBytes(32));
TokenType tokenType = new TokenType(randomBytes(32));
byte[] tokenData = "token immutable data".getBytes(StandardCharsets.UTF_8);
TokenCoinData coinData = new TokenCoinData(
    Map.of(
        new CoinId("coin".getBytes()), BigInteger.valueOf(100),
        new CoinId("second coin".getBytes()), BigInteger.valueOf(5)
    )
);

// Create predicate for initial state and use its reference as address
byte[] nonce = randomBytes(32);
// Create key pair from nonce and secret
SigningService signingService = SigningService.createFromMaskedSecret(secret, nonce);
MaskedPredicate predicate = MaskedPredicate.create(
    tokenId,
    tokenType,
    signingService,
    HashAlgorithm.SHA256,
    nonce
);

byte[] salt = randomBytes(32);
MintCommitment<?> commitment = MintCommitment.create(
    new MintTransaction.Data<>(
        tokenId,
        tokenType,
        tokenData,
        coinData,
        predicate.getReference().toAddress(),
        salt,
        null,
        null
    )
);

// Submit mint transaction using StateTransitionClient
SubmitCommitmentResponse response = client
    .submitCommitment(commitment)
    .get();

if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
    throw new Exception(
        String.format(
            "Failed to submit mint commitment: %s", 
            response.getStatus()
        )
    );
}

// Wait for inclusion proof
InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
    client,
    trustBase,
    commitment
).get();

// Create mint transaction
Token<?> token = Token.create(
    trustBase,
    // Create initial state with transaction data
    new TokenState(predicate, null),
    commitment.toTransaction(inclusionProof)
);
```

### Get Block Height

```java
Long blockHeight = client.getAggregatorClient().getBlockHeight().get();
System.out.println("Current block height: "+blockHeight);
```

### Transfer a Token

```java
byte[] senderSecret = secret;
byte[] senderNonce = nonce;

String recipientNametag = "RECIPIENT";
byte[] recipientData = "Custom data".getBytes(StandardCharsets.UTF_8);
DataHash recipientDataHash = new DataHasher(HashAlgorithm.SHA256)
    .update(recipientData)
    .digest();

byte[] salt = randomBytes(32);

// Submit transfer transaction
TransferCommitment transferCommitment = TransferCommitment.create(
    token,
    ProxyAddress.create(recipientNametag),
    salt,
    recipientDataHash,
    null,
    SigningService.createFromMaskedSecret(senderSecret, senderNonce)
);

SubmitCommitmentResponse transferResponse = this.client.submitCommitment(transferCommitment).get();
if (transferResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
    throw new Exception(
      String.format(
        "Failed to submit transfer commitment: %s",
        transferResponse.getStatus()
      )
    );
}

// Create transfer transaction
TransferTransaction transferTransaction = transferCommitment.toTransaction(
    InclusionProofUtils.waitInclusionProof(
        client,
        trustBase,
        transferCommitment
    ).get()
);

// Prepare info for sending to recipient
String transferTransactionJson = transferTransaction.toJson();
String tokenJson = token.toJson();
```

### Receive the token

```java
String recipientNametag = "RECIPIENT";
byte[] receiverSecret = "RECEIVER_SECRET".getBytes();

Token<?> token = Token.fromJson("TOKEN JSON");
TransferTransaction transaction = TransferTransaction.fromJson("TRANSFER TRANSACTION JSON");

// Create nametag token
TokenType nametagType = new TokenType(randomBytes(32));
byte[] nametagNonce = randomBytes(32);
byte[] nametagSalt = randomBytes(32);

MintCommitment<?> nametagMintCommitment = MintCommitment.create(
    new MintTransaction.NametagData(
        recipientNametag,
        nametagType,
        MaskedPredicateReference.create(
            nametagType,
            SigningService.createFromMaskedSecret(receiverSecret, nametagNonce),
            HashAlgorithm.SHA256,
            nametagNonce
        ).toAddress(),
        nametagSalt,
        UnmaskedPredicateReference.create(
            token.getType(),
            SigningService.createFromSecret(receiverSecret),
            HashAlgorithm.SHA256
        ).toAddress()
    )
);

// Submit nametag mint transaction using StateTransitionClient
SubmitCommitmentResponse nametagMintResponse = client.submitCommitment(nametagMintCommitment).get();
if (nametagMintResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
    throw new Exception(
        String.format(
            "Failed to submit nametag mint commitment: %s",
            nametagMintResponse.getStatus()
        )
    );
}

// Wait for inclusion proof
InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
    client,
    trustBase,
    nametagMintCommitment
).get();

Token<?> nametagToken = Token.create(
    trustBase,
    new TokenState(
        MaskedPredicate.create(
            nametagMintCommitment.getTransactionData().getTokenId(),
            nametagMintCommitment.getTransactionData().getTokenType(),
            SigningService.createFromMaskedSecret(receiverSecret, nametagNonce),
            HashAlgorithm.SHA256,
            nametagNonce
        ),
        null
    ),
    nametagMintCommitment.toTransaction(inclusionProof)
);

// Receiver finalizes the token
Token<?> finalizedToken = client.finalizeTransaction(
    trustBase,
    token,
    new TokenState(
        UnmaskedPredicate.create(
            token.getId(),
            token.getType(),
            SigningService.createFromSecret(receiverSecret),
            HashAlgorithm.SHA256,
            transaction.getData().getSalt()
        ),
        null
    ),
    transaction,
    List.of(nametagToken)
);

```

## Building from Source

### Clone the Repository

```bash
git clone https://github.com/unicitynetwork/java-state-transition-sdk.git
cd java-state-transition-sdk
```

### Build the Project

```bash
./gradlew build
```

### Run Tests

```bash
# Run unit tests
./gradlew test

# Run integration tests (requires Docker)
./gradlew integrationTest

# Run E2E tests against deployed aggregator
AGGREGATOR_URL=https://gateway-test.unicity.network ./gradlew integrationTest
```

## Platform-Specific Considerations

### Android Compatibility

The SDK is compatible with Android 12+ (API level 31+). It uses:

- OkHttp for HTTP operations instead of Java 11's HttpClient
- Android-compatible Guava version
- Animal Sniffer plugin to ensure API compatibility

### JVM Compatibility

The standard JVM version uses:

- Java 11 APIs
- Full Guava JRE version
- All Java 11 features

## Architecture

The SDK follows a modular architecture under `org.unicitylabs.sdk`:

- **api**: Core API interfaces and aggregator client
- **address**: Address schemes and implementations (DirectAddress, ProxyAddress)
- **bft**: BFT layer data, trustbase, certificates, seals
- **hash**: Cryptographic hashing (SHA256, SHA224, SHA384, SHA512, RIPEMD160)
- **jsonrpc**: JSON-RPC transport layer
- **`mtree`**: Merkle tree implementations
    - `plain`: Sparse Merkle Tree (SMT)
    - `sum`: Sparse Merkle Sum Tree (SMST)
- **predicate**: Ownership predicates
- **serializer**: CBOR/JSON serializer utilities
- **signing**: Digital signature support (ECDSA secp256k1)
- **token**: Token types
    - `fungible`: Fungible token support with CoinId and TokenCoinData
      **`transaction`**: Transaction types and builders
    - `split`: Token splitting functionality with TokenSplitBuilder
- **util**: Utilities
- **verification**: Verification rules

All certificate and transaction verification is handled internally by the SDK, requiring only the
trustbase as input from the user.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java naming conventions
- Add unit tests for new features
- Update documentation as needed
- Ensure compatibility with both JVM and Android platforms
- Run `./gradlew build` before submitting PR

## Testing

The SDK includes comprehensive test suites:

### Unit Tests

Located in `src/test/java`, these test individual components in isolation.

### Integration Tests

Located in `src/test/java/org/unicitylabs/sdk/`:

- `integration/TokenIntegrationTest`: Tests against Docker-based local aggregator
- `e2e/TokenE2ETest`: E2E tests using CommonTestFlow (requires `AGGREGATOR_URL` env var)
- `e2e/BasicE2ETest`: Basic connectivity and performance tests

### Running Tests

```bash
# All tests
./gradlew test

# Integration tests only
./gradlew integrationTest

# Specific test class
./gradlew test --tests "org.unicitylabs.sdk.api.RequestIdTest"
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For issues and feature requests, please use
the [GitHub issue tracker](https://github.com/unicitynetwork/java-state-transition-sdk/issues).

For questions about the Unicity Labs, visit [unicity-labs.com](https://unicity-labs.com).

## Acknowledgments

- Built on the Unicity network protocol
- Uses Jackson for CBOR encoding
- Uses Bouncy Castle for cryptographic operations
- Uses OkHttp for Android-compatible HTTP operations