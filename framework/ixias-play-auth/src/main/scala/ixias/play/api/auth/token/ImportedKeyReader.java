package ixias.play.api.auth.token;

import org.keyczar.*;
import org.keyczar.enums.KeyPurpose;
import org.keyczar.enums.KeyStatus;
import org.keyczar.exceptions.KeyczarException;
import org.keyczar.interfaces.KeyczarReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Copy from org.keyczar.ImportedKeyReader, which has package private constructor only.
 */
public class ImportedKeyReader implements KeyczarReader {
    private final KeyMetadata metadata;
    private final List<KeyczarKey> keys;

    public ImportedKeyReader(KeyMetadata metadata, List<KeyczarKey> keys) {
        this.metadata = metadata;
        this.keys = keys;
    }

    public ImportedKeyReader(AesKey key) {
        this.metadata = new KeyMetadata(
                "Imported AES", KeyPurpose.DECRYPT_AND_ENCRYPT, DefaultKeyType.AES);
        KeyVersion version = new KeyVersion(0, KeyStatus.PRIMARY, false);
        this.metadata.addVersion(version);
        this.keys = new ArrayList<KeyczarKey>();
        this.keys.add(key);
    }

    public ImportedKeyReader(HmacKey key) {
        this.metadata = new KeyMetadata(
                "Imported HMAC", KeyPurpose.SIGN_AND_VERIFY, DefaultKeyType.HMAC_SHA1);
        KeyVersion version = new KeyVersion(0, KeyStatus.PRIMARY, false);
        this.metadata.addVersion(version);
        this.keys = new ArrayList<KeyczarKey>();
        this.keys.add(key);
    }

    @Override
    public String getKey() throws KeyczarException {
        KeyMetadata metadata = KeyMetadata.read(getMetadata());

        return getKey(metadata.getPrimaryVersion().getVersionNumber());
    }

    @Override
    public String getKey(int version) {
        return keys.get(version).toString();
    }

    @Override
    public String getMetadata() {
        return metadata.toString();
    }
}
