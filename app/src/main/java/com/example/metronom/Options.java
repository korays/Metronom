package com.example.metronom;

public class Options {

	private boolean beatOn;
	private boolean isVibtare;
	private boolean isFlash;
	private boolean isSound;
	private int bpm;

	public Options() {
	}

	public boolean isBeatOn() {
		return beatOn;
	}

	public void setBeatOn(boolean beatOn) {
		this.beatOn = beatOn;
	}

	public boolean isVibtare() {
		return isVibtare;
	}

	public void setIsVibtare(boolean isVibtare) {
		this.isVibtare = isVibtare;
	}

	public boolean isFlash() {
		return isFlash;
	}

	public void setIsFlash(boolean isFlash) {
		this.isFlash = isFlash;
	}

	public boolean isSound() {
		return isSound;
	}

	public void setIsSound(boolean isSound) {
		this.isSound = isSound;
	}

	public int getBpm() {
		return bpm;
	}

	public void setBpm(int bpm) {
		this.bpm = bpm;
	}
}
