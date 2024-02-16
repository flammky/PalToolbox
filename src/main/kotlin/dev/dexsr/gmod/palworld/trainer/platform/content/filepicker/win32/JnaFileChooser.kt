package dev.dexsr.gmod.palworld.trainer.platform.content.filepicker.win32

import com.sun.jna.Platform
import java.awt.Window
import java.io.File
import java.util.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


/**
 * JnaFileChooser is a wrapper around the native Windows file chooser
 * and folder browser that falls back to the Swing JFileChooser on platforms
 * other than Windows or if the user chooses a combination of features
 * that are not supported by the native dialogs (for example multiple
 * selection of directories).
 *
 * Example:
 * JnaFileChooser fc = new JnaFileChooser();
 * fc.setFilter("All Files", "*");
 * fc.setFilter("Pictures", "jpg", "jpeg", "gif", "png", "bmp");
 * fc.setMultiSelectionEnabled(true);
 * fc.setMode(JnaFileChooser.Mode.FilesAndDirectories);
 * if (fc.showOpenDialog(parent)) {
 * Files[] selected = fc.getSelectedFiles();
 * // do something with selected
 * }
 *
 * @see JFileChooser, WindowsFileChooser, WindowsFileBrowser
 */
class JnaFileChooser
    () {
    private enum class Action {
        Open, Save
    }

    /**
     * the availabe selection modes of the dialog
     */
    enum class Mode(val jFileChooserValue: Int) {
        Files(JFileChooser.FILES_ONLY),
        Directories(JFileChooser.DIRECTORIES_ONLY),
        FilesAndDirectories(JFileChooser.FILES_AND_DIRECTORIES)
    }

    var selectedFiles: Array<File?>
        protected set
    var currentDirectory: File? = null
        protected set
    protected var filters: ArrayList<Array<String>> = ArrayList()

    /**
     * sets whether to enable multiselection
     *
     * @param enabled true to enable multiselection, false to disable it
     */
    var isMultiSelectionEnabled: Boolean = false

    /**
     * sets the selection mode
     *
     * @param mode the selection mode
     */
    var mode: Mode = Mode.Files

    protected var _defaultFile: String = ""
    protected var _dialogTitle: String = ""
    protected var _openButtonText: String = ""
    protected var _saveButtonText: String = ""

    /**
     * creates a new file chooser with multiselection disabled and mode set
     * to allow file selection only.
     */
    init {
        selectedFiles = arrayOf(null)
    }

    /**
     * creates a new file chooser with the specified initial directory
     *
     * @param currentDirectory the initial directory
     */
    constructor(currentDirectory: File?) : this() {
        if (currentDirectory != null) {
            this.currentDirectory = if (currentDirectory.isDirectory) currentDirectory else currentDirectory.parentFile
        }
    }

    /**
     * creates a new file chooser with the specified initial directory
     *
     * @param currentDirectory the initial directory
     */
    constructor(currentDirectoryPath: String?) : this(if (currentDirectoryPath != null) File(currentDirectoryPath) else null)

    /**
     * shows a dialog for opening files
     *
     * @param parent the parent window
     *
     * @return true if the user clicked OK
     */
    fun showOpenDialog(parent: Window): Boolean {
        return showDialog(parent, Action.Open)
    }

    /**
     * shows a dialog for saving files
     *
     * @param parent the parent window
     *
     * @return true if the user clicked OK
     */
    fun showSaveDialog(parent: Window): Boolean {
        return showDialog(parent, Action.Save)
    }

    private fun showDialog(parent: Window, action: Action): Boolean {
        // native windows filechooser doesn't support mixed selection mode
        if (Platform.isWindows() && mode != Mode.FilesAndDirectories) {
            // windows filechooser can only multiselect files
            if (isMultiSelectionEnabled && mode == Mode.Files) {
                // TODO Here we would use the native windows dialog
                // to choose multiple files. However I haven't been able
                // to get it to work properly yet because it requires
                // tricky callback magic and somehow this didn't work for me
                // quite as documented (probably because I messed something up).
                // Because I don't need this feature right now I've put it on
                // hold to get on with stuff.
                // Example code: http://support.microsoft.com/kb/131462/en-us
                // GetOpenFileName: http://msdn.microsoft.com/en-us/library/ms646927.aspx
                // OFNHookProc: http://msdn.microsoft.com/en-us/library/ms646931.aspx
                // CDN_SELCHANGE: http://msdn.microsoft.com/en-us/library/ms646865.aspx
                // SendMessage: http://msdn.microsoft.com/en-us/library/ms644950.aspx
            } else if (!isMultiSelectionEnabled) {
                if (mode == Mode.Files) {
                    return showWindowsFileChooser(parent, action)
                } else if (mode == Mode.Directories) {
                    return showWindowsFolderBrowser(parent)
                }
            }
        }

        // fallback to Swing
        return showSwingFileChooser(parent, action)
    }

    private fun showSwingFileChooser(parent: Window, action: Action): Boolean {
        val fc = JFileChooser(currentDirectory)
        fc.isMultiSelectionEnabled = isMultiSelectionEnabled
        fc.fileSelectionMode = mode.jFileChooserValue

        // set select file
        if (!_defaultFile.isEmpty() and (action == Action.Save)) {
            val fsel = File(_defaultFile)
            fc.selectedFile = fsel
        }
        if (!_dialogTitle.isEmpty()) {
            fc.dialogTitle = _dialogTitle
        }
        if ((action == Action.Open) and !_openButtonText.isEmpty()) {
            fc.approveButtonText = _openButtonText
        } else if ((action == Action.Save) and !_saveButtonText.isEmpty()) {
            fc.approveButtonText = _saveButtonText
        }

        // build filters
        if (filters.size > 0) {
            var useAcceptAllFilter = false
            for (spec in filters) {
                // the "All Files" filter is handled specially by JFileChooser
                if (spec[1] == "*") {
                    useAcceptAllFilter = true
                    continue
                }
                fc.addChoosableFileFilter(
                    FileNameExtensionFilter(
                        spec[0], *Arrays.copyOfRange(spec, 1, spec.size)
                    )
                )
            }
            fc.isAcceptAllFileFilterUsed = useAcceptAllFilter
        }

        var result = -1
        result = if (action == Action.Open) {
            fc.showOpenDialog(parent)
        } else {
            if (_saveButtonText.isEmpty()) {
                fc.showSaveDialog(parent)
            } else {
                fc.showDialog(parent, null)
            }
        }
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFiles = if (isMultiSelectionEnabled) fc.selectedFiles else arrayOf(fc.selectedFile)
            currentDirectory = fc.currentDirectory
            return true
        }

        return false
    }

    val windowsFileChooser = WindowsFileChooser()
    private fun showWindowsFileChooser(parent: Window, action: Action): Boolean {
        windowsFileChooser.setFilters(filters)

        if (!_defaultFile.isEmpty()) windowsFileChooser.setDefaultFilename(_defaultFile)

        if (!_dialogTitle.isEmpty()) {
            windowsFileChooser.setTitle(_dialogTitle)
        }

        val result = windowsFileChooser.showDialog(parent, action == Action.Open)
        if (result) {
            selectedFiles = arrayOf(windowsFileChooser.selectedFile)
            currentDirectory = windowsFileChooser.currentDirectory
        }
        return result
    }

    private fun showWindowsFolderBrowser(parent: Window): Boolean {
        val fb = WindowsFolderBrowser()
        if (!_dialogTitle.isEmpty()) {
            fb.setTitle(_dialogTitle)
        }
        val file = fb.showDialog(parent)
        if (file != null) {
            selectedFiles = arrayOf(file)
            currentDirectory = if (file.parentFile != null) file.parentFile else file
            return true
        }

        return false
    }

    /**
     * add a filter to the user-selectable list of file filters
     *
     * @param name   name of the filter
     * @param filter you must pass at least 1 argument, the arguments are the file
     * extensions.
     */
    fun addFilter(name: String, vararg filter: String) {
        require(filter.size >= 1)
        val parts = ArrayList<String>()
        parts.add(name)
        Collections.addAll(parts, *filter)
        filters.add(parts.toTypedArray<String>())
    }

    fun setCurrentDirectory(currentDirectoryPath: String?) {
        this.currentDirectory = (if (currentDirectoryPath != null) File(currentDirectoryPath) else null)
    }

    fun setDefaultFileName(dfile: String) {
        this._defaultFile = dfile
    }

    /**
     * set a title name
     *
     * @param Title of dialog
     */
    fun setTitle(title: String) {
        this._dialogTitle = title
    }

    /**
     * set a open button name
     *
     * @param open button text
     */
    fun setOpenButtonText(buttonText: String) {
        this._openButtonText = buttonText
    }

    /**
     * set a save button name
     *
     * @param save button text
     */
    fun setSaveButtonText(buttonText: String) {
        this._saveButtonText = buttonText
    }

    val selectedFile: File?
        get() = selectedFiles[0]
}