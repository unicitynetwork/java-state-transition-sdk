package com.unicity.sdk.transaction;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;

public class MintTransactionData implements ISerializable {
    private final TokenId tokenId;
    private final TokenType tokenType;
    private final ISerializable tokenData;
    private final TokenCoinData coinData;

    public MintTransactionData(TokenId tokenId, TokenType tokenType, ISerializable tokenData, TokenCoinData coinData) {
        this.tokenId = tokenId;
        this.tokenType = tokenType;
        this.tokenData = tokenData;
        this.coinData = coinData;
    }

    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public ISerializable getTokenData() {
        return tokenData;
    }

    public TokenCoinData getCoinData() {
        return coinData;
    }

    @Override
    public Object toJSON() {
        return this;
    }

    @Override
    public byte[] toCBOR() {
        return new byte[0];
    }
}