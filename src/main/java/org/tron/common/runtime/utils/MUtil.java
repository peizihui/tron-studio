package org.tron.common.runtime.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.Hash;
import org.tron.common.storage.Deposit;
import org.tron.core.Wallet;
import org.tron.core.actuator.TransferActuator;
import org.tron.core.actuator.TransferAssetActuator;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.config.args.Account;
import org.tron.core.exception.ContractValidateException;
import org.tron.protos.Protocol;

public class MUtil {
  private MUtil() {}

  public static void transfer(Deposit deposit, byte[] fromAddress, byte[] toAddress, long amount)
      throws ContractValidateException {
    if (0 == amount) {
      return;
    }
    TransferActuator.validateForSmartContract(deposit, fromAddress, toAddress, amount);
    deposit.addBalance(toAddress, amount);
    deposit.addBalance(fromAddress, -amount);
  }

  public static void transferAllToken(Deposit deposit, byte[] fromAddress, byte[] toAddress) {
    AccountCapsule fromAccountCap = deposit.getAccount(fromAddress);
    Protocol.Account.Builder fromBuilder = fromAccountCap.getInstance().toBuilder();
    AccountCapsule toAccountCap = deposit.getAccount(toAddress);
    Protocol.Account.Builder toBuilder = toAccountCap.getInstance().toBuilder();
    fromAccountCap.getAssetMapV2().forEach((tokenId, amount) -> {
      toBuilder.putAssetV2(tokenId,toBuilder.getAssetV2Map().getOrDefault(tokenId, 0L) + amount);
      fromBuilder.putAssetV2(tokenId,0L);
    });
    deposit.putAccountValue(fromAddress,new AccountCapsule(fromBuilder.build()));
    deposit.putAccountValue(toAddress, new AccountCapsule(toBuilder.build()));
  }

  public static void transferToken(Deposit deposit, byte[] fromAddress, byte[] toAddress, String tokenId, long amount)
      throws ContractValidateException {
    if (0 == amount) {
      return;
    }
    TransferAssetActuator.validateForSmartContract(deposit, fromAddress, toAddress, tokenId.getBytes(), amount);
    deposit.addTokenBalance(toAddress, tokenId.getBytes(), amount);
    deposit.addTokenBalance(fromAddress, tokenId.getBytes(), -amount);
  }

  public static byte[] convertToTronAddress(byte[] address) {
    if (address.length == 20) {
      byte[] newAddress = new byte[21];
      byte[] temp = new byte[]{Wallet.getAddressPreFixByte()};
      System.arraycopy(temp, 0, newAddress, 0, temp.length);
      System.arraycopy(address, 0, newAddress, temp.length, address.length);
      address = newAddress;
    }
    return address;
  }

  public static byte[] convertFromTronAddress(byte[] address) {
    if (address.length == 21) {
      byte[] newAddress = new byte[20];
      System.arraycopy(address, 1, newAddress, 0, 20);
      address = newAddress;
    }
    return address;
  }

  public static String get4BytesSha3HexString(String data) {
    return Hex.toHexString(Arrays.copyOf(Hash.sha3(data.getBytes()), 4));
  }

  public static byte[] generateByteArray(byte[] ...parameters){
    int length =0;
    for(int i=0;i<parameters.length;i++){
      length+=parameters[i].length;
    }
    byte[] result = new byte[length];
    int pos =0;
    for (int i=0;i<parameters.length;i++){
      System.arraycopy(parameters[i],0,result,pos,parameters[i].length);
      pos += parameters[i].length;
    }
    return result;
  }

}
