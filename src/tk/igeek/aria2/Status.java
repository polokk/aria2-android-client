package tk.igeek.aria2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.R.string;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Status extends CommonItem implements Parcelable{

	public Status(HashMap<String, Object> data) {
		init(data);
		getFiles();
		if(bittorrent != null)
		{
			bittorrentInfo = new BitTorrent(bittorrent);
		}
	}
	
	/**
	 * GID of this download.
	 */
	public String gid = "";

	/**
	 * "active" for currently downloading/seeding entry. "waiting" for the entry
	 * in the queue; download is not started. "paused" for the paused entry.
	 * "error" for the stopped download because of error. "complete" for the
	 * stopped and completed download. "removed" for the download removed by
	 * user.
	 */
	public String status = "";

	/**
	 * 
	 Total length of this download in bytes.
	 */
	public String totalLength = "";

	/**
	 * 
	 Completed length of this download in bytes.
	 */
	public String completedLength = "";

	/**
	 * 
	 Uploaded length of this download in bytes.
	 */
	public String uploadLength = "";

	/**
	 * Hexadecimal representation of the download progress. The highest bit
	 * corresponds to piece index 0. The set bits indicate the piece is
	 * available and unset bits indicate the piece is missing. The spare bits at
	 * the end are set to zero. When download has not started yet, this key will
	 * not be included in the response.
	 */

	public String bitfield = "";

	/**
	 * 
	 Download speed of this download measured in bytes/sec.
	 */
	public String downloadSpeed = "";

	/**
	 * 
	 Upload speed of this download measured in bytes/sec.
	 */
	public String uploadSpeed = "";

	/**
	 * 
	 InfoHash. BitTorrent only.
	 */
	public String infoHash = "";

	/**
	 * 
	 The number of seeders the client has connected to. BitTorrent only.
	 */
	public String numSeeders = "";

	/**
	 * 
	 Piece length in bytes.
	 */
	public String pieceLength = "";

	/**
	 * 
	 The number of pieces.
	 */
	public String numPieces = "";

	/**
	 * 
	 The number of peers/servers the client has connected to.
	 */
	public String connections = "";

	/**
	 * The last error code occurred in this download. The value is of type
	 * string. The error codes are defined in EXIT STATUS section. This value is
	 * only available for stopped/completed downloads.
	 */
	public String errorCode = "";

	/**
	 * List of GIDs which are generated by the consequence of this download. For
	 * example, when aria2 downloaded Metalink file, it generates downloads
	 * described in it(see --follow-metalink option). This value is useful to
	 * track these auto generated downloads. If there is no such downloads, this
	 * key will not be included in the response.
	 */
	public Object[] followedBy = null;

	/**
	 * GID of a parent download. Some downloads are a part of another download.
	 * For example, if a file in Metalink has BitTorrent resource, the download
	 * of .torrent is a part of that file. If this download has no parent, this
	 * key will not be included in the response.
	 */
	public String belongsTo = "";

	/**
	 * Directory to save files. This key is not available for stopped downloads.
	 */
	public String dir = "";

	/**
	 * Returns the list of files. The element of list is the same struct used in
	 * aria2.getFiles method.
	 */
	public Object[] files = null;
    public ArrayList<Files> filesList = new ArrayList<Files>();
	
	/**
	 * Struct which contains information retrieved from 
	 * .torrent file. BitTorrent only. It contains following keys.
	 */
	public HashMap<String, Object> bittorrent = null;
	public BitTorrent bittorrentInfo = null;
	
	
	enum DOWNLOAD_TYPE{UNKNOWN,HTTP_FTP,TORRENT,METALINK};
	
	public String getName()
	{
		String name = "unknow";
		DOWNLOAD_TYPE downloadType = DOWNLOAD_TYPE.HTTP_FTP;
		
		Log.i("getName", "numSeeders:" + numSeeders);
		
		if(!numSeeders.equals(""))
		{
			downloadType = DOWNLOAD_TYPE.TORRENT;
		}
		
		switch(downloadType)
		{
		case HTTP_FTP:
			if(filesList.size() > 0)
			{
				name = filesList.get(0).path;
				if(name.equals("") && filesList.get(0).urisList.size() > 0)
				{
					name = filesList.get(0).urisList.get(0).uri;
				}
			}
			break;
		case TORRENT:
		case METALINK:
			if(bittorrentInfo != null)
			{
				if(bittorrentInfo.mode.equals("multi"))
				{
					name = (String)bittorrentInfo.info.get("name");
				}else
				{
					name = filesList.get(0).path;
				}
				
			}else {
				name = filesList.get(0).path;
			}
			break;
			
		}
		
		return name;
	}
	
	
	
	
	
	
	public void getFiles()
	{
		if(files == null)
		{
			return;
		}
		for (Object file: files)
		{
			Files fileItem =new Files((HashMap<String, Object>)file);
			filesList.add(fileItem);
		}
	}
	
	public static final Parcelable.Creator<Status> CREATOR =
			new Parcelable.Creator<Status>(){

	    @Override
	    public Status createFromParcel(Parcel source) {
	     return new Status(source);
	    }
	
	    @Override
	    public Status[] newArray(int size) {
	     return new Status[size];
	    }
	};
	
	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		write(dest); 
		
	}
	
	 private void readFromParcel(Parcel in) 
	 {    
		read(in); 
     }  
	 
	 public Status(Parcel source) {
		 readFromParcel(source);
	 }
	 
	 public void write(Parcel dest) {
		Field[] fields = getClass().getFields();

		try {
			for (Field field : fields) {
				if (field.getModifiers() == Modifier.PUBLIC) {
					
					Object value = field.get(this);

					if(field.getType() == String.class)
					{
						dest.writeString((String) value);
					}else if(field.getName().equals("files"))
					{
						dest.writeTypedList(filesList);
					}
					
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	 
	 
	
	 
	 public void read(Parcel in) {
		 
		Field[] fields = getClass().getFields();
		try {
			for (Field field : fields) {
				if (field.getModifiers() == Modifier.PUBLIC) {
					if(field.getType() == String.class)
					{
						field.set(this,in.readString());
					}else if(field.getName().equals("files"))
					{
						in.readTypedList(filesList,Files.CREATOR);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	
}
