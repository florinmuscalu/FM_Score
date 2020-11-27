package ro.florinm.FM_Score;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class FM_ScorePlayer {
    private String do_a = "";
    private String re_a = "";
    private String mi_a = "";
    private String fa_a = "";
    private String sol_a = "";
    private String la_a = "";
    private String si_a = "";
    private FM_Audio_Song song;
    public FM_SoundPool soundPlayer;
    boolean playing_step, playing;
    Context context;

    public FM_ScorePlayer(Context context) {
        super();
        this.context = context;
        soundPlayer = new FM_SoundPool(context);
        playing = false;
        playing_step = false;
    }

    public int LoadFromJson(JSONObject obj) {
        try {
            song = generateHarmonicSong(obj.optString("keysignature", "DO"), obj.getJSONArray("keys"));
        } catch (JSONException e) {
            return -1;
        }
        return 0;
    }

    public static FM_Audio_Song generateHarmonicSong(String keysignature, JSONArray keys) {
        FM_Audio_Song song = new FM_Audio_Song();
        song.keysignature = keysignature;
        JSONArray a = new JSONArray();
        try {
            while (keys.length() > 0) {
                JSONArray b = (JSONArray) keys.get(0);
                if (b.getString(0).startsWith("BAR")) {
                    JSONArray value = new JSONArray();
                    value.put("BAR");
                    a.put(value);
                    keys.remove(0);
                    continue;
                }
                boolean still_to_go = true;
                int current_group = 0;
                while (still_to_go) {
                    int i = 0;
                    current_group = current_group + 1;
                    JSONArray value = new JSONArray();
                    while (true) {
                        if (keys.length() == 0) {
                            still_to_go = false;
                            break;
                        }
                        if (i > keys.length() - 1) {
                            break;
                        }
                        JSONArray tmp = (JSONArray) keys.get(i);
                        if (tmp.getString(0).startsWith("BAR")) {
                            if (i == 0) still_to_go = false;
                            break;
                        }
                        int pg = tmp.getInt(8);
                        if (current_group == pg) {
                            value.put(tmp.getString(0));
                            value.put(tmp.getString(1));
                            keys.remove(i);
                            i = i - 1;
                        }
                        i = i + 1;
                    }
                    a.put(value);
                }
            }

            FM_Audio_Measure m = new FM_Audio_Measure();
            song.measures.add(m);
            for (int i = 0; i < a.length(); i++) {
                JSONArray b = (JSONArray) a.get(i);
                if (b.length() == 1 && b.getString(0).startsWith("BAR")) {
                    m = new FM_Audio_Measure();
                    song.measures.add(m);
                } else {
                    FM_Audio_Note n = new FM_Audio_Note();
                    String note = "";
                    String duration = "";
                    for (int j = 0; j < b.length(); j = j + 2) {
                        note = note + "," + b.getString(j);
                        duration = duration + "," + b.getString(j + 1);
                    }
                    n.note = note.substring(1);
                    n.duration = duration.substring(1);
                    n.playDuration = duration.substring(1);
                    n.pauseDuration = duration.substring(1);
                    m.notes.add(n);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return song;
    }

    public void Play(int tempo) {
        Play(tempo, 1, song.measures.size(), 0, false);
    }

    public void Prepare(int tempo) {
        Play(tempo,1, song.measures.size(), 0, true);
    }

    private void Play(int tempo, int measure_start, int measure_end) {
        Play(tempo, measure_start, measure_end, 0, false);
    }

    private void Prepare(int tempo, int measure_start, int measure_end) {
        Play(tempo, measure_start, measure_end, 0, true);
    }

    private void Play(int tempo, int measure_start, int measure_end, int notes) {
        Play(tempo, measure_start, measure_end, notes, false);
    }

    private void Prepare(int tempo, int measure_start, int measure_end, int notes) {
        Play(tempo, measure_start, measure_end, notes, true);
    }

    private void Play(int tempo, int measure_start, int measure_end, int notes, Boolean prepare) {
        if (prepare) {
            song.prepared = false;
            PlayHarmonic(tempo, song, measure_start, measure_end, notes, prepare);
        } else {
            new Thread(() -> {
                while (!song.prepared)
                    try {
                        sleep(10);
                    } catch (Exception ignored) {
                    }
                playing = true;
                PlayHarmonic(tempo, song, measure_start, measure_end, notes, prepare);
            }).start();
        }
    }

    private void PlayHarmonic(int tempo, final FM_Audio_Song song, int measure_start, int measure_end, int notes, Boolean prepare) {
        soundPlayer.TEMPO = 1000 * (60/tempo);
        if (measure_end > song.measures.size()) measure_end = song.measures.size();
        if (measure_end == song.measures.size()) notes = 0;
        List<FM_Audio_Note> ListNotes = new ArrayList<>();
        for (int i = measure_start - 1; i < measure_end; i++) {
            for (int j = 0; j < song.measures.get(i).notes.size(); j++) {
                String[] n = song.measures.get(i).notes.get(j).note.split(",");
                n = computeNote(song.keysignature, n);
                String[] d = song.measures.get(i).notes.get(j).duration.split(",");
                ListNotes.add(song.measures.get(i).notes.get(j));
                if (n.length == 1) {
                    ListNotes.get(ListNotes.size() - 1).audioINT = soundPlayer.GetIndex(n[0].trim());
                    ListNotes.get(ListNotes.size() - 1).audioT = null;
                    ListNotes.get(ListNotes.size() - 1).playDuration = d[0];
                    ListNotes.get(ListNotes.size() - 1).pauseDuration = d[0];
                } else {
                    List<Integer> tracks = new ArrayList<>();
                    for (String s : n)
                        tracks.add(soundPlayer.GetIndex(s.trim()));
                    ListNotes.get(ListNotes.size() - 1).audioINT = 0;
                    ListNotes.get(ListNotes.size() - 1).audioT = null;
                    ListNotes.get(ListNotes.size() - 1).pauseDuration = d[0];
                    ListNotes.get(ListNotes.size() - 1).playDuration = d[0];
                    for (String s : d) {
                        if (soundPlayer.GetDurationFromStr(ListNotes.get(ListNotes.size() - 1).pauseDuration) > soundPlayer.GetDurationFromStr(s))
                            ListNotes.get(ListNotes.size() - 1).pauseDuration = s;
                        if (soundPlayer.GetDurationFromStr(ListNotes.get(ListNotes.size() - 1).playDuration) < soundPlayer.GetDurationFromStr(s))
                            ListNotes.get(ListNotes.size() - 1).playDuration = s;
                    }
                    if (tracks.size() == 2)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), null, null, null, null, null);
                    if (tracks.size() == 3)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), new FM_AudioSubTrack(tracks.get(2), soundPlayer.GetDurationFromStr(d[2])), null, null, null, null);
                    if (tracks.size() == 4)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), new FM_AudioSubTrack(tracks.get(2), soundPlayer.GetDurationFromStr(d[2])), new FM_AudioSubTrack(tracks.get(3), soundPlayer.GetDurationFromStr(d[3])), null, null, null);
                    if (tracks.size() == 5)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), new FM_AudioSubTrack(tracks.get(2), soundPlayer.GetDurationFromStr(d[2])), new FM_AudioSubTrack(tracks.get(3), soundPlayer.GetDurationFromStr(d[3])), new FM_AudioSubTrack(tracks.get(4), soundPlayer.GetDurationFromStr(d[4])), null, null);
                    if (tracks.size() == 6)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), new FM_AudioSubTrack(tracks.get(2), soundPlayer.GetDurationFromStr(d[2])), new FM_AudioSubTrack(tracks.get(3), soundPlayer.GetDurationFromStr(d[3])), new FM_AudioSubTrack(tracks.get(4), soundPlayer.GetDurationFromStr(d[4])), new FM_AudioSubTrack(tracks.get(5), soundPlayer.GetDurationFromStr(d[5])), null);
                    if (tracks.size() == 7)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), new FM_AudioSubTrack(tracks.get(2), soundPlayer.GetDurationFromStr(d[2])), new FM_AudioSubTrack(tracks.get(3), soundPlayer.GetDurationFromStr(d[3])), new FM_AudioSubTrack(tracks.get(4), soundPlayer.GetDurationFromStr(d[4])), new FM_AudioSubTrack(tracks.get(5), soundPlayer.GetDurationFromStr(d[5])), new FM_AudioSubTrack(tracks.get(6), soundPlayer.GetDurationFromStr(d[6])));
                }
            }
        }
        if (notes != 0)
            for (int j = 0; j < notes; j++) {
                String[] n = song.measures.get(measure_end).notes.get(j).note.split(",");
                n = computeNote(song.keysignature, n);
                String[] d = song.measures.get(measure_end).notes.get(j).duration.split(",");
                ListNotes.add(song.measures.get(measure_end).notes.get(j));
                if (n.length == 1) {
                    ListNotes.get(ListNotes.size() - 1).audioINT = soundPlayer.GetIndex(n[0].trim());
                    ListNotes.get(ListNotes.size() - 1).audioT = null;
                    ListNotes.get(ListNotes.size() - 1).playDuration = d[0];
                    ListNotes.get(ListNotes.size() - 1).pauseDuration = d[0];
                } else {
                    List<Integer> tracks = new ArrayList<>();
                    for (String s : n)
                        tracks.add(soundPlayer.GetIndex(s.trim()));
                    ListNotes.get(ListNotes.size() - 1).audioINT = 0;
                    ListNotes.get(ListNotes.size() - 1).audioT = null;
                    ListNotes.get(ListNotes.size() - 1).pauseDuration = d[0];
                    ListNotes.get(ListNotes.size() - 1).playDuration = d[0];
                    for (String s : d) {
                        if (soundPlayer.GetDurationFromStr(ListNotes.get(ListNotes.size() - 1).pauseDuration) > soundPlayer.GetDurationFromStr(s))
                            ListNotes.get(ListNotes.size() - 1).pauseDuration = s;
                        if (soundPlayer.GetDurationFromStr(ListNotes.get(ListNotes.size() - 1).playDuration) < soundPlayer.GetDurationFromStr(s))
                            ListNotes.get(ListNotes.size() - 1).playDuration = s;
                    }
                    if (tracks.size() == 2)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), null, null, null, null, null);
                    if (tracks.size() == 3)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), new FM_AudioSubTrack(tracks.get(2), soundPlayer.GetDurationFromStr(d[2])), null, null, null, null);
                    if (tracks.size() == 4)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), new FM_AudioSubTrack(tracks.get(2), soundPlayer.GetDurationFromStr(d[2])), new FM_AudioSubTrack(tracks.get(3), soundPlayer.GetDurationFromStr(d[3])), null, null, null);
                    if (tracks.size() == 5)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), new FM_AudioSubTrack(tracks.get(2), soundPlayer.GetDurationFromStr(d[2])), new FM_AudioSubTrack(tracks.get(3), soundPlayer.GetDurationFromStr(d[3])), new FM_AudioSubTrack(tracks.get(4), soundPlayer.GetDurationFromStr(d[4])), null, null);
                    if (tracks.size() == 6)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), new FM_AudioSubTrack(tracks.get(2), soundPlayer.GetDurationFromStr(d[2])), new FM_AudioSubTrack(tracks.get(3), soundPlayer.GetDurationFromStr(d[3])), new FM_AudioSubTrack(tracks.get(4), soundPlayer.GetDurationFromStr(d[4])), new FM_AudioSubTrack(tracks.get(5), soundPlayer.GetDurationFromStr(d[5])), null);
                    if (tracks.size() == 7)
                        ListNotes.get(ListNotes.size() - 1).audioT = soundPlayer.CreateTrack(new FM_AudioSubTrack(tracks.get(0), soundPlayer.GetDurationFromStr(d[0])), new FM_AudioSubTrack(tracks.get(1), soundPlayer.GetDurationFromStr(d[1])), new FM_AudioSubTrack(tracks.get(2), soundPlayer.GetDurationFromStr(d[2])), new FM_AudioSubTrack(tracks.get(3), soundPlayer.GetDurationFromStr(d[3])), new FM_AudioSubTrack(tracks.get(4), soundPlayer.GetDurationFromStr(d[4])), new FM_AudioSubTrack(tracks.get(5), soundPlayer.GetDurationFromStr(d[5])), new FM_AudioSubTrack(tracks.get(6), soundPlayer.GetDurationFromStr(d[6])));
                }
            }

        for (int i = 1; i < ListNotes.size(); i++) if (ListNotes.get(i).audioINT == -1) ListNotes.get(i - 1).NextPause = true;

        song.prepared = true;
        if (!prepare)
            new Thread(() -> {
                playing_step = true;
                for (FM_Audio_Note n : ListNotes) {
                    if (!playing) continue;
                    if (n.audioINT != 0) {
                        soundPlayer.playKey(n.audioINT, n.NextPause);
                        soundPlayer.SleepHarmonic(n.playDuration);
                        soundPlayer.stopKey(n.audioINT);
                    } else {
                        n.audioT.Play(soundPlayer.GetDurationFromStr(n.playDuration), n.NextPause);
                        FM_SoundPool.SleepHarmonic(soundPlayer.GetDurationFromStr(n.pauseDuration));
                    }
                }
                playing_step = false;
            }).start();
    }

    public void StopPlaying() {
        playing = false;
    }

    private String[] computeNote(String tonality, String[] n) {
        String[] ret = new String[n.length];
        for (int i = 0; i < n.length; i++) {
            String o = n[i].substring(n[i].length() - 2);
            String note = n[i].substring(0, n[i].length() - 2);
            String initial_note = note;
            String tmp = ApplyTonality(tonality, note);
            if (note.startsWith("DO")) {
                if (!note.equals("DO")) do_a = note;
                else note = do_a;
            }
            if (note.startsWith("RE")) {
                if (!note.equals("RE")) re_a = note;
                else note = re_a;
            }
            if (note.startsWith("MI")) {
                if (!note.equals("MI")) mi_a = note;
                else note = mi_a;
            }
            if (note.startsWith("FA")) {
                if (!note.equals("FA")) fa_a = note;
                else note = fa_a;
            }
            if (note.startsWith("SOL")) {
                if (!note.equals("SOL")) sol_a = note;
                else note = sol_a;
            }
            if (note.startsWith("LA")) {
                if (!note.equals("LA")) la_a = note;
                else note = la_a;
            }
            if (note.startsWith("SI")) {
                if (!note.equals("SI")) si_a = note;
                else note = si_a;
            }
            if (initial_note.equals(note)) note = tmp;
            ret[i] = note + o;
        }
        return ret;
    }

    private String ApplyTonality(String tonality, String key){
        //send it only the note, without the octave!!
        if (key.contains("n") || key.contains("#") || key.contains("##") || key.contains("b") || key.contains("bb") || key.equals("B")) return key;
        String k = key.toUpperCase();
        if (tonality.equals("SOL") || tonality.equals("MIm")){
            if (k.equals("FA")) k = k+"#";
        }
        if (tonality.equals("RE") || tonality.equals("SIm")){
            if (k.equals("FA") || k.equals("DO")) k = k + "#";
        }
        if (tonality.equals("LA") || tonality.equals("FA#m")){
            if (k.equals("FA") || k.equals("DO")|| k.equals("SOL")) k = k + "#";
        }
        if (tonality.equals("MI") || tonality.equals("DO#m")){
            if (k.equals("FA") || k.equals("DO") || k.equals("SOL") || k.equals("RE")) k = k + "#";
        }
        if (tonality.equals("SI") || tonality.equals("SOL#m")){
            if (k.equals("FA") || k.equals("DO") || k.equals("SOL") || k.equals("RE") || k.equals("LA")) k = k + "#";
        }
        if (tonality.equals("FA") || tonality.equals("REm")){
            if (k.equals("SI")) k = k+ "b";
        }
        if (tonality.equals("SIb") || tonality.equals("SOLm")){
            if (k.equals("SI") || k.equals("MI")) k = k + "b";
        }
        if (tonality.equals("MIb") || tonality.equals("DOm")){
            if (k.equals("SI") || k.equals("MI")|| k.equals("LA")) k = k + "b";
        }
        if (tonality.equals("LAb") || tonality.equals("FAm")){
            if (k.equals("SI") || k.equals("MI") || k.equals("LA") || k.equals("RE")) k = k + "b";
        }
        if (tonality.equals("REb") || tonality.equals("SIbm")){
            if (k.equals("SI") || k.equals("MI") || k.equals("LA") || k.equals("RE") || k.equals("SOL")) k = k + "b";
        }
        return k;
    }

    private void StartMeasure() {
        do_a = "DO";
        re_a = "RE";
        mi_a = "MI";
        fa_a = "FA";
        sol_a = "SOL";
        la_a = "LA";
        si_a = "SI";
    }
}