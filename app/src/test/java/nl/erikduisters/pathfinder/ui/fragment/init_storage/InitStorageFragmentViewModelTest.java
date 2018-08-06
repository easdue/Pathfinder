package nl.erikduisters.pathfinder.ui.fragment.init_storage;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.local.StorageHelper;

import static org.mockito.Mockito.when;

/**
 * Created by Erik Duisters on 07-06-2018.
 */
@RunWith(JUnit4.class)
public class InitStorageFragmentViewModelTest {
    //private PreferenceManager preferenceManager = mock(PreferenceManager.class);
    //private StorageHelper storageHelper = mock(StorageHelper.class);

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Observer<InitStorageFragmentViewState> observer;
    @Mock
    private PreferenceManager preferenceManager;
    @Mock
    private StorageHelper storageHelper;

    private InitStorageFragmentViewModel initStorageFragmentViewModel;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void storageIsAdopted_resultsShowFatalMessageDialogState() {
        when(preferenceManager.getStorageDir()).thenReturn("");

        File storage = new File(StorageHelper.ADOPTED_STORAGE_PREFIX, "12345");
        when(storageHelper.getFilesDir()).thenReturn(storage);
        when(storageHelper.isStorageAdopted(storage)).thenReturn(true);

        InitStorageFragmentViewModel initStorageFragmentViewModel = new InitStorageFragmentViewModel(preferenceManager, storageHelper);
        LiveData<InitStorageFragmentViewState> liveData = initStorageFragmentViewModel.getViewState();

        assert(liveData.getValue() instanceof InitStorageFragmentViewState.ShowFatalMessageDialogState);
    }

/* TODO: Write more tests but then I'll have to use PowerMockito or convert FileUtil to not be a static utility class
    @Test
    public void noExternalStorageAvailable_resultsInShowSpaceRequirementWarningState() {
        when(preferenceManager.getStorageDir()).thenReturn("");

        //File storage = new File("/storage/16EE-2302/Android/data/nl.erikduisters.pathfinder/files");
        File storage = new File("/data/user/0/nl.erikduisters.pathfinder/files");
        when(storageHelper.getFilesDir()).thenReturn(storage);

        File cache = new File("/data/user/0/nl.erikduisters.pathfinder/cache");
        when(storageHelper.getCacheDir()).thenReturn(cache);

        File[] external = new File[] {null};
        when(storageHelper.getExternalFilesDirs()).thenReturn(external);
        when(storageHelper.getExternalCacheDirs()).thenReturn(external);

        InitStorageFragmentViewModel initStorageFragmentViewModel = new InitStorageFragmentViewModel(preferenceManager, storageHelper);
        LiveData<InitStorageFragmentViewState> liveData = initStorageFragmentViewModel.getViewState();

        assert(liveData.getValue() instanceof InitStorageFragmentViewState.ShowSpaceRequirementWarningState);
    }
*/
    // /storage/emulated/0/Android/data/nl.erikduisters.pathfinder/files
    // /storage/16EE-2302/Android/data/nl.erikduisters.pathfinder/files
}