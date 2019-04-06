package kr.ac.gachon.www.SaveMe.Entity;

public class TTS {  //TTS의 볼륨과 메세지를 담은 객체
    int volume;
    String msg;

     public TTS(int volume, String msg) {
         this.volume=volume;
         this.msg=msg;
     }

    public int getVolume() {
        return volume;
    }

    public String getMsg() {
        return msg;
    }
}
