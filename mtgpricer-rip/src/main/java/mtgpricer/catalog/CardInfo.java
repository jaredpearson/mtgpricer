package mtgpricer.catalog;

/**
 * Represents the card information data that can be serialized/deserialized
 * @author jared.pearson
 */
class CardInfo {
	private final String name;
	private final String number;
	private final Integer multiverseid;
	private final String artist;
	private final String imageName;
	
	public CardInfo(String name, String number, Integer multiverseId, String artist, String imageName) {
		this.name = name;
		this.number = number;
		this.multiverseid = multiverseId;
		this.artist = artist;
		this.imageName = imageName;
	}
	
	public String getName() {
		return name;
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
	
	String getImageName() {
		return imageName;
	}
}