package net.ximias.fileSearch;

import java.io.File;
import java.util.HashSet;

public interface ResultCallback {
	void onCompleted(HashSet<File> files);
}
