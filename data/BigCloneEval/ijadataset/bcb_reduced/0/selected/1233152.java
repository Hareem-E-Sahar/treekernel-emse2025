package org.jampa.runnable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.jampa.controllers.Controller;
import org.jampa.gui.translations.Messages;
import org.jampa.logging.Log;
import org.jampa.model.playlists.Playlist;
import org.jampa.utils.SystemUtils;
import org.jampa.utils.SystemUtils.PlaylistFormat;

public class PlaylistExporter implements IRunnableWithProgress {

    private String _path;

    private boolean _isDirectory;

    private List<String> _playlists;

    private PlaylistFormat _format;

    public PlaylistExporter(String path, PlaylistFormat format, boolean isDirectory, List<String> playlists) {
        _path = path;
        _isDirectory = isDirectory;
        _playlists = playlists;
        _format = format;
    }

    public void writeSelectedPlaylistsToZip(IProgressMonitor monitor) {
        File tmpFile = new File(_path);
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
        byte[] buf = new byte[1024];
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(_path));
            Playlist playlist;
            InputStream in;
            String name;
            Iterator<String> iter = _playlists.iterator();
            while (iter.hasNext()) {
                if (monitor.isCanceled()) {
                    out.close();
                    return;
                }
                playlist = Controller.getInstance().getPlaylistController().getPlaylistByName(iter.next());
                monitor.subTask(Messages.getString("PlaylistExporter.ExportPlaylist") + " " + playlist.getName());
                if (playlist != null) {
                    if (_format == PlaylistFormat.XSPF) {
                        name = playlist.getName() + SystemUtils.playlistXSPFExtension;
                        in = playlist.getPlaylistStreamAsXSPF();
                    } else {
                        name = playlist.getName() + SystemUtils.playlistM3UExtension;
                        in = playlist.getPlaylistStreamAsM3U();
                    }
                    out.putNextEntry(new ZipEntry(name));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                    monitor.worked(1);
                }
            }
            out.close();
        } catch (Exception e) {
            Log.getInstance(PlaylistExporter.class).error("Error while writting file : " + _path);
        }
    }

    private void writeSelectedPlaylistsToDirectory(IProgressMonitor monitor) {
        Playlist playlist;
        Iterator<String> iter = _playlists.iterator();
        while (iter.hasNext()) {
            if (monitor.isCanceled()) return;
            playlist = Controller.getInstance().getPlaylistController().getPlaylistByName(iter.next());
            monitor.subTask(Messages.getString("PlaylistExporter.ExportPlaylist") + " " + playlist.getName());
            if (playlist != null) {
                playlist.writePlaylistToDirectory(_path, _format);
            }
            monitor.worked(1);
        }
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask(Messages.getString("PlaylistExporter.PlaylistExportTitle"), _playlists.size());
        if (_isDirectory) {
            writeSelectedPlaylistsToDirectory(monitor);
        } else {
            writeSelectedPlaylistsToZip(monitor);
        }
        monitor.done();
    }
}
