package net.ximias.fileSearch;

import java.io.File;
import java.util.HashSet;

public interface resultCallback{
	void onCompleted(HashSet<File> files);
}
