package org.mobicents.protocols.ss7.map.smstpdu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPParsingComponentException;
import org.mobicents.protocols.ss7.map.api.smstpdu.AddressField;
import org.mobicents.protocols.ss7.map.api.smstpdu.NumberingPlanIdentification;
import org.mobicents.protocols.ss7.map.api.smstpdu.TypeOfNumber;
import org.mobicents.protocols.ss7.map.primitives.TbcdString;

/**
 * 
 * @author sergey vetyutnev
 * 
 */
public class AddressFieldImpl implements AddressField {

    private TypeOfNumber typeOfNumber;

    private NumberingPlanIdentification numberingPlanIdentification;

    private String addressValue;

    private AddressFieldImpl() {
    }

    public AddressFieldImpl(TypeOfNumber typeOfNumber, NumberingPlanIdentification numberingPlanIdentification, String addressValue) {
        this.typeOfNumber = typeOfNumber;
        this.numberingPlanIdentification = numberingPlanIdentification;
        this.addressValue = addressValue;
    }

    public static AddressFieldImpl createMessage(InputStream stm) throws MAPException {
        if (stm == null) throw new MAPException("Error creating AddressField: stream must not be null");
        AddressFieldImpl res = new AddressFieldImpl();
        try {
            int addressLength = stm.read();
            if (addressLength == -1) throw new MAPException("Error creating AddressField: Address-Length field not found");
            if (addressLength < 0 || addressLength > 20) throw new MAPException("Error creating AddressField: Address-Length field must be equal from 0 to 20, found: addressLength");
            int addressArrayLength = (addressLength + 1) / 2;
            int typeOfAddress = stm.read();
            if (typeOfAddress == -1) throw new MAPException("Error creating AddressField: Type-of-Address field not found");
            res.typeOfNumber = TypeOfNumber.getInstance((typeOfAddress & 0x70) >> 4);
            res.numberingPlanIdentification = NumberingPlanIdentification.getInstance(typeOfAddress & 0x0F);
            res.addressValue = TbcdString.decodeString(stm, addressArrayLength);
        } catch (IOException e) {
            throw new MAPException("IOException when creating AddressField: " + e.getMessage(), e);
        } catch (MAPParsingComponentException e) {
            throw new MAPException("MAPParsingComponentException when creating AddressField: " + e.getMessage(), e);
        }
        return res;
    }

    @Override
    public TypeOfNumber getTypeOfNumber() {
        return this.typeOfNumber;
    }

    @Override
    public NumberingPlanIdentification getNumberingPlanIdentification() {
        return this.numberingPlanIdentification;
    }

    @Override
    public String getAddressValue() {
        return this.addressValue;
    }

    @Override
    public void encodeData(OutputStream stm) throws MAPException {
        if (typeOfNumber == null || numberingPlanIdentification == null || addressValue == null) throw new MAPException("Error encoding AddressFieldImpl: typeOfNumber, addressValue and numberingPlanIdentification fields must not be null");
        try {
            int addrLen = addressValue.length();
            int tpOfAddr = 0x80 + (this.typeOfNumber.getCode() << 4) + this.numberingPlanIdentification.getCode();
            stm.write(addrLen);
            stm.write(tpOfAddr);
            TbcdString.encodeString(stm, addressValue);
        } catch (IOException e) {
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AddressField [");
        if (typeOfNumber != null) {
            sb.append("typeOfNumber=");
            sb.append(typeOfNumber);
        }
        if (numberingPlanIdentification != null) {
            sb.append(", numberingPlanIdentification=");
            sb.append(numberingPlanIdentification);
        }
        if (addressValue != null) {
            sb.append(", addressValue=");
            sb.append(addressValue);
        }
        sb.append("]");
        return sb.toString();
    }
}
