package mo.livestream.rtmplive;

public interface MOLiveStreamCallBack {
	public void onConnecting();
    public void onConnected();
    public void onDisconnect();
    public void onConnectError(int err);
}
