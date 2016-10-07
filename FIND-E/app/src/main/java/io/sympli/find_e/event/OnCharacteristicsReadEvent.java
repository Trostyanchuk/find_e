package io.sympli.find_e.event;

public class OnCharacteristicsReadEvent {

    private CharacteristicValue characteristicValue;

    public OnCharacteristicsReadEvent(CharacteristicValue value) {
        this.characteristicValue = value;
    }

    public CharacteristicValue getCharacteristicValue() {
        return characteristicValue;
    }
}
