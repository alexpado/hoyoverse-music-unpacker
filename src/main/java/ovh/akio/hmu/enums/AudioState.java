package ovh.akio.hmu.enums;

public enum AudioState {

    CREATED('C'),
    UPDATED('U'),
    UNCHANGED('-'),
    DUPLICATED('D');

    final char flag;

    AudioState(char flag) {

        this.flag = flag;
    }

    public char getFlag() {

        return this.flag;
    }
}
