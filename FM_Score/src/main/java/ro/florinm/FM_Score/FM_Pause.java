package ro.florinm.FM_Score;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class FM_Pause extends FM_BaseNote {
    @FM_DurationValue
    private int duration;

    public FM_Pause(FM_Score Stave, @FM_DurationValue int duration) {
        super(FM_NoteType.PAUSE, Stave);
        this.duration = duration;
    }

    public float getDisplacement() {
        if (duration == 1 || duration == 51) return 1.0f;
        if (duration == 2 || duration == 52) return 2.0f;
        if (duration == 4 || duration == 54) return 2.0f;
        if (duration == 8 || duration == 58) return 2.0f;
        if (duration == 16 || duration == 516) return 1.0f;
        if (duration == 32 || duration == 532) return 2.0f;

        return 2.0f;
    }

    public String toString() {
        String s = "";
        if (duration == 1 || duration == 51) return FM_Const.Pause_1;
        if (duration == 2 || duration == 52) return FM_Const.Pause_2;
        if (duration == 4 || duration == 54) return FM_Const.Pause_4;
        if (duration == 8 || duration == 58) return FM_Const.Pause_8;
        if (duration == 16 || duration == 516) return FM_Const.Pause_16;
        if (duration == 32 || duration == 532) return FM_Const.Pause_32;
        return s;
    }

    private int lineSpan() {
        int s = 0;
        if (duration == 1 || duration == 51) return 1;
        if (duration == 2 || duration == 52) return 1;
        if (duration == 4 || duration == 54) return 3;
        if (duration == 8 || duration == 58) return 2;
        if (duration == 16 || duration == 516) return 3;
        if (duration == 32 || duration == 532) return 4;
        return s;
    }

    @Override
    public float WidthAll(Paint font, boolean all) {
        return WidthAll(font);
    }

    public float WidthAll(Paint font) {
        FM_Const.AdjustFont(context, font, FM_Const.Pause_8, Stave.getDistanceBetweenStaveLines(), 2);
        return padding + font.measureText(toString()) + paddingExtra;
    }

    public float WidthAccidental(Paint font) {
        return 0;
    }

    public float WidthAllNoDot(Paint font) {
        return WidthAll(font);
    }
    public float WidthNote(Paint font) {
        return WidthAll(font);
    }

    private float BottomMargin(Paint font) {
        FM_Const.AdjustFont(context, font, toString(), Stave.getDistanceBetweenStaveLines(), lineSpan());
        Rect bounds = new Rect();
        String s = toString();
        font.getTextBounds(s, 0, s.length(), bounds);
        return bounds.bottom;
    }

    private float TopMargin(Paint font) {
        FM_Const.AdjustFont(context, font, toString(), Stave.getDistanceBetweenStaveLines(), lineSpan());
        Rect bounds = new Rect();
        String s = toString();
        font.getTextBounds(s, 0, s.length(), bounds);
        return bounds.top;
    }

    public void DrawNote(Canvas canvas) {
        if (!isVisible()) return;
        super.DrawNote(canvas);

        Stave.StaveFont.setColor(Color);
        FM_Const.AdjustFont(context, Stave.StaveFont, FM_Const.Pause_8, Stave.getDistanceBetweenStaveLines(),2);
        canvas.drawText(toString(), StartX + padding, StartY1 + getDisplacement() * Stave.getDistanceBetweenStaveLines(), Stave.StaveFont);
    }
    public float Left(){
        return StartX + padding;
    };
    public float Bottom() {
        return StartY1 + getDisplacement() * Stave.getDistanceBetweenStaveLines() + BottomMargin(Stave.StaveFont);
    }
    public float Right() {
        return StartX + WidthAll(Stave.StaveFont);
    }
    public float Top(){
        return StartY1 + getDisplacement() * Stave.getDistanceBetweenStaveLines() + TopMargin(Stave.StaveFont);
    }
}