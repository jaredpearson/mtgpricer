package mtgpricer.catalog;

/**
 * Represents a printing of a card within the catalog of Magic The Gathering cards. 
 * @author jared.pearson
 */
public class Card {
	private final String name;
	private final String setCode;
	private final String number;
	private final Integer multiverseid;
	private final String artist;
	private final String imageName;
	private final boolean validInStandard;
	
	public Card(String setCode, CardInfo cardInfo, boolean validInStandard) {
		this.setCode = setCode;
		this.name = cardInfo.getName();
		this.number = cardInfo.getNumber();
		this.multiverseid = cardInfo.getMultiverseId();
		this.artist = cardInfo.getArtist();
		this.imageName = cardInfo.getImageName();
		this.validInStandard = validInStandard;
	}
	
	public Card(String name, String setCode, String number, Integer multiverseId, String artist, String imageName, boolean validInStandard) {
		this.name = name;
		this.setCode = setCode;
		this.number = number;
		this.multiverseid = multiverseId;
		this.artist = artist;
		this.imageName = imageName;
		this.validInStandard = validInStandard;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSetCode() {
		return setCode;
	}
	
	public String getNumber() {
		return number;
	}

	public Integer getMultiverseId() {
		return this.multiverseid;
	}
	
	public String getArtist() {
		return artist;
	}

	/**
	 * Determines if the card is generally valid to be played in tournament format.
	 */
	public boolean isValidInStandardTournamentFormat() {
		return validInStandard;
	}
	
	String getImageName() {
		return imageName;
	}
}