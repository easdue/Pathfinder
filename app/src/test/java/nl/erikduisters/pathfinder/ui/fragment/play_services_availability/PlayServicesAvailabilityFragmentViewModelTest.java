package nl.erikduisters.pathfinder.ui.fragment.play_services_availability;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.fragment.play_services_availability.PlayServicesAvailabilityFragmentViewState.AskUserToResolveUnavailabilityState;
import nl.erikduisters.pathfinder.ui.fragment.play_services_availability.PlayServicesAvailabilityFragmentViewState.ReportPlayServicesAvailabilityState;
import nl.erikduisters.pathfinder.ui.fragment.play_services_availability.PlayServicesAvailabilityFragmentViewState.WaitForPlayServicesUpdateState;
import nl.erikduisters.pathfinder.ui.fragment.play_services_availability.PlayServicesAvailabilityFragmentViewState.WaitingForUserToResolveUnavailabilityState;
import nl.erikduisters.pathfinder.ui.fragment.play_services_availability.PlayServicesHelper.ServiceState;
import nl.erikduisters.pathfinder.util.MainThreadExecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Erik Duisters on 22-06-2018.
 */
@RunWith(JUnit4.class)
public class PlayServicesAvailabilityFragmentViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    MainThreadExecutor mainThreadExecutor;
    @Mock
    PlayServicesHelper playServicesHelper;

    private PlayServicesAvailabilityFragmentViewModel viewModel;
    private LiveData<PlayServicesAvailabilityFragmentViewState> viewStateObservable;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        viewModel = new PlayServicesAvailabilityFragmentViewModel(mainThreadExecutor);
        viewStateObservable = viewModel.getViewStateObservable();
    }

    @Test
    public void noPlayServicesHelperSetWhenCallingCheckPlayServicesAvailability_resultsInRuntimeException() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("You have to call setPlayServicesHelper() before using any other function of this class");

        viewModel.checkPlayServicesAvailability();
    }

    @Test
    public void whenServicesStateIsOkWhenCallingCheckPlayServicesAvailability_resultsInReportPlayServicesAvailabilityState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_OK);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        verify(playServicesHelper, times(1)).getGooglePlayServicesState();
        assertTrue(viewStateObservable.getValue() instanceof ReportPlayServicesAvailabilityState);
        ReportPlayServicesAvailabilityState state = (ReportPlayServicesAvailabilityState) viewStateObservable.getValue();
        assertTrue(state.googlePlayServicesIsAvailable);
    }

    @Test
    public void whenServiceStateIsDisabledWhenCallingCheckPlayServicesAvailability_resultsInAskUserToResolveUnavailabilityState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_DISABLED);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_DISABLED)).thenReturn(true);
        when(playServicesHelper.getDialogTitle(ServiceState.SERVICE_DISABLED)).thenReturn(1001);
        when(playServicesHelper.getDialogMessage(ServiceState.SERVICE_DISABLED)).thenReturn(1002);
        when(playServicesHelper.getDialogPositiveButtonText(ServiceState.SERVICE_DISABLED)).thenReturn(1003);
        when(playServicesHelper.getDialogNegativeButtonText(ServiceState.SERVICE_DISABLED)).thenReturn(1004);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        verify(playServicesHelper, times(1)).getGooglePlayServicesState();
        verify(playServicesHelper, times(1)).isStateUserResolvable(ServiceState.SERVICE_DISABLED);

        assertTrue(viewStateObservable.getValue() instanceof AskUserToResolveUnavailabilityState);
        AskUserToResolveUnavailabilityState state = (AskUserToResolveUnavailabilityState) viewStateObservable.getValue();
        assertEquals(playServicesHelper.getDialogTitle(ServiceState.SERVICE_DISABLED), state.messageWithTitle.titleResId);
        assertEquals(playServicesHelper.getDialogMessage(ServiceState.SERVICE_DISABLED), state.messageWithTitle.messageResId);
        assertEquals(playServicesHelper.getDialogPositiveButtonText(ServiceState.SERVICE_DISABLED), state.positiveButtonTextResId);
        assertEquals(playServicesHelper.getDialogNegativeButtonText(ServiceState.SERVICE_DISABLED), state.negativeButtonTextResId);
    }

    @Test
    public void whenServiceStateIsMissingWhenCallingCheckPlayServicesAvailability_resultsInAskUserToResolveUnavailabilityState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_MISSING);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_MISSING)).thenReturn(true);
        when(playServicesHelper.getDialogTitle(ServiceState.SERVICE_MISSING)).thenReturn(1001);
        when(playServicesHelper.getDialogMessage(ServiceState.SERVICE_MISSING)).thenReturn(1002);
        when(playServicesHelper.getDialogPositiveButtonText(ServiceState.SERVICE_MISSING)).thenReturn(1003);
        when(playServicesHelper.getDialogNegativeButtonText(ServiceState.SERVICE_MISSING)).thenReturn(1004);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        verify(playServicesHelper, times(1)).getGooglePlayServicesState();
        verify(playServicesHelper, times(1)).isStateUserResolvable(ServiceState.SERVICE_MISSING);

        assertTrue(viewStateObservable.getValue() instanceof AskUserToResolveUnavailabilityState);
        AskUserToResolveUnavailabilityState state = (AskUserToResolveUnavailabilityState) viewStateObservable.getValue();
        assertEquals(playServicesHelper.getDialogTitle(ServiceState.SERVICE_MISSING), state.messageWithTitle.titleResId);
        assertEquals(playServicesHelper.getDialogMessage(ServiceState.SERVICE_MISSING), state.messageWithTitle.messageResId);
        assertEquals(playServicesHelper.getDialogPositiveButtonText(ServiceState.SERVICE_MISSING), state.positiveButtonTextResId);
        assertEquals(playServicesHelper.getDialogNegativeButtonText(ServiceState.SERVICE_MISSING), state.negativeButtonTextResId);
    }

    @Test
    public void whenServiceStateIsUpdateRequiredWhenCallingCheckPlayServicesAvailability_resultsInAskUserToResolveUnavailabilityState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_UPDATE_REQUIRED);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_UPDATE_REQUIRED)).thenReturn(true);
        when(playServicesHelper.getDialogTitle(ServiceState.SERVICE_UPDATE_REQUIRED)).thenReturn(1001);
        when(playServicesHelper.getDialogMessage(ServiceState.SERVICE_UPDATE_REQUIRED)).thenReturn(1002);
        when(playServicesHelper.getDialogPositiveButtonText(ServiceState.SERVICE_UPDATE_REQUIRED)).thenReturn(1003);
        when(playServicesHelper.getDialogNegativeButtonText(ServiceState.SERVICE_UPDATE_REQUIRED)).thenReturn(1004);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        verify(playServicesHelper, times(1)).getGooglePlayServicesState();
        verify(playServicesHelper, times(1)).isStateUserResolvable(ServiceState.SERVICE_UPDATE_REQUIRED);

        assertTrue(viewStateObservable.getValue() instanceof AskUserToResolveUnavailabilityState);
        AskUserToResolveUnavailabilityState state = (AskUserToResolveUnavailabilityState) viewStateObservable.getValue();
        assertEquals(playServicesHelper.getDialogTitle(ServiceState.SERVICE_UPDATE_REQUIRED), state.messageWithTitle.titleResId);
        assertEquals(playServicesHelper.getDialogMessage(ServiceState.SERVICE_UPDATE_REQUIRED), state.messageWithTitle.messageResId);
        assertEquals(playServicesHelper.getDialogPositiveButtonText(ServiceState.SERVICE_UPDATE_REQUIRED), state.positiveButtonTextResId);
        assertEquals(playServicesHelper.getDialogNegativeButtonText(ServiceState.SERVICE_UPDATE_REQUIRED), state.negativeButtonTextResId);
    }

    @Test
    public void whenServiceStateIsUpdatingWhenCallingCheckPlayServicesAvailability_resultsInAskUserToResolveUnavailabilityState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_UPDATING);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_UPDATING)).thenReturn(true);
        when(playServicesHelper.getDialogTitle(ServiceState.SERVICE_UPDATING)).thenReturn(1001);
        when(playServicesHelper.getDialogMessage(ServiceState.SERVICE_UPDATING)).thenReturn(1002);
        when(playServicesHelper.getDialogPositiveButtonText(ServiceState.SERVICE_UPDATING)).thenReturn(1003);
        when(playServicesHelper.getDialogNegativeButtonText(ServiceState.SERVICE_UPDATING)).thenReturn(1004);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        verify(playServicesHelper, times(1)).getGooglePlayServicesState();
        verify(playServicesHelper, times(1)).isStateUserResolvable(ServiceState.SERVICE_UPDATING);

        assertTrue(viewStateObservable.getValue() instanceof AskUserToResolveUnavailabilityState);
        AskUserToResolveUnavailabilityState state = (AskUserToResolveUnavailabilityState) viewStateObservable.getValue();
        assertEquals(playServicesHelper.getDialogTitle(ServiceState.SERVICE_UPDATING), state.messageWithTitle.titleResId);
        assertEquals(playServicesHelper.getDialogMessage(ServiceState.SERVICE_UPDATING), state.messageWithTitle.messageResId);
        assertEquals(playServicesHelper.getDialogPositiveButtonText(ServiceState.SERVICE_UPDATING), state.positiveButtonTextResId);
        assertEquals(playServicesHelper.getDialogNegativeButtonText(ServiceState.SERVICE_UPDATING), state.negativeButtonTextResId);
    }

    @Test
    public void whenServiceStateIsInvalidWhenCallingCheckPlayServicesAvailability_resultsInReportPlayServicesAvailabilityState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_INVALID);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_INVALID)).thenReturn(false);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        assertTrue(viewStateObservable.getValue() instanceof ReportPlayServicesAvailabilityState);
        ReportPlayServicesAvailabilityState state = (ReportPlayServicesAvailabilityState) viewStateObservable.getValue();
        assertFalse(state.googlePlayServicesIsAvailable);
    }

    @Test
    public void whenViewStateIsAskUserToResolveUnavailabilityStateWhenCallingCheckPlayServicesAvailability_resultsInNoStateChange() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_DISABLED);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_DISABLED)).thenReturn(true);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        PlayServicesAvailabilityFragmentViewState prevState = viewStateObservable.getValue();

        assertTrue(prevState instanceof AskUserToResolveUnavailabilityState);
        viewModel.checkPlayServicesAvailability();

        assertTrue(prevState == viewStateObservable.getValue());
    }

    @Test
    public void whenViewStateIsWaitingForUserToResolveUnavailabilityStateWhenCallingCheckPlayServicesAvailability_resultsInNoStateChange() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_DISABLED);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_DISABLED)).thenReturn(true);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();
        viewModel.onUserWantsToResolveUnavailabilityState();

        PlayServicesAvailabilityFragmentViewState prevState = viewStateObservable.getValue();

        assertTrue(prevState instanceof WaitingForUserToResolveUnavailabilityState);
        viewModel.checkPlayServicesAvailability();

        assertTrue(prevState == viewStateObservable.getValue());
    }

    @Test
    public void whenViewStateIsWaitForPlayServicesUpdateStateWhenCallingCheckPlayServicesAvailability_resultsInNoStateChange() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_UPDATING);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_DISABLED)).thenReturn(true);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();
        viewModel.onUserWantsToResolveUnavailabilityState();

        PlayServicesAvailabilityFragmentViewState prevState = viewStateObservable.getValue();

        assertTrue(prevState instanceof WaitForPlayServicesUpdateState);
        viewModel.checkPlayServicesAvailability();

        assertTrue(prevState == viewStateObservable.getValue());
    }

    @Test
    public void whenOnPlayServicesAvailabilityStateReportedIsCalled_resultsInNullState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_OK);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        assertTrue(viewStateObservable.getValue() != null);
        viewModel.onPlayServicesAvailabilityStateReported();
        assertTrue(viewStateObservable.getValue() == null);
    }

    @Test
    public void whenOnUserWantsToResolveUnavailabilityStateIsCalledWithoutSettingPlayServicesHelper_resultsInRuntimeException() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("You have to call setPlayServicesHelper() before using any other function of this class");

        viewModel.onUserWantsToResolveUnavailabilityState();
    }

    @Test
    public void whenOnUserWantsToResolveUnavailabilityStateIsCalledWithStateDisabled_resultsInWaitingForUserToResolveUnavailabilityState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_DISABLED);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_DISABLED)).thenReturn(true);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        assertTrue(viewStateObservable.getValue() instanceof AskUserToResolveUnavailabilityState);

        viewModel.onUserWantsToResolveUnavailabilityState();
        verify(playServicesHelper, times(1)).tryToResolveUnavailabilityState(ServiceState.SERVICE_DISABLED);
        assertTrue(viewStateObservable.getValue() instanceof WaitingForUserToResolveUnavailabilityState);
    }

    @Test
    public void whenOnUserWantsToResolveUnavailabilityStateIsCalledWithStateMissing_resultsInWaitingForUserToResolveUnavailabilityState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_MISSING);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_MISSING)).thenReturn(true);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        assertTrue(viewStateObservable.getValue() instanceof AskUserToResolveUnavailabilityState);

        viewModel.onUserWantsToResolveUnavailabilityState();
        verify(playServicesHelper, times(1)).tryToResolveUnavailabilityState(ServiceState.SERVICE_MISSING);
        assertTrue(viewStateObservable.getValue() instanceof WaitingForUserToResolveUnavailabilityState);
    }

    @Test
    public void whenOnUserWantsToResolveUnavailabilityStateIsCalledWithStateUpdateRequired_resultsInWaitingForUserToResolveUnavailabilityState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_UPDATE_REQUIRED);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_UPDATE_REQUIRED)).thenReturn(true);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        assertTrue(viewStateObservable.getValue() instanceof AskUserToResolveUnavailabilityState);

        viewModel.onUserWantsToResolveUnavailabilityState();
        verify(playServicesHelper, times(1)).tryToResolveUnavailabilityState(ServiceState.SERVICE_UPDATE_REQUIRED);
        assertTrue(viewStateObservable.getValue() instanceof WaitingForUserToResolveUnavailabilityState);
    }

    @Test
    public void whenOnUserWantsToResolveUnavailabilityStateIsCalledWithStatusUpdating_resultsInWaitForPlayServicesUpdateState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_UPDATING);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_UPDATING)).thenReturn(true);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        assertTrue(viewStateObservable.getValue() instanceof AskUserToResolveUnavailabilityState);

        viewModel.onUserWantsToResolveUnavailabilityState();

        assertTrue(viewStateObservable.getValue() instanceof WaitForPlayServicesUpdateState);
        WaitForPlayServicesUpdateState state = (WaitForPlayServicesUpdateState) viewStateObservable.getValue();
        ProgressDialog.Properties properties = state.dialogProperties;
        assertEquals(0, properties.getTitleResId());
        assertEquals(android.R.string.cancel, properties.getPositiveButtonTextResId());
        assertFalse(properties.showHorizontalProgressBar());
        assertTrue(properties.isProgressBarIndeterminate());
        assertFalse(properties.isCancelable());
    }

    @Test
    public void whenViewstateIsWaitForPlayServicesUpdateStateAndPlayServicesStateBecomesAvailable_resultsInReportPlayServicesAvailabilityState() {
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_UPDATING);
        when(playServicesHelper.isStateUserResolvable(ServiceState.SERVICE_UPDATING)).thenReturn(true);

        viewModel.setPlayServicesHelper(playServicesHelper);
        viewModel.checkPlayServicesAvailability();

        viewModel.onUserWantsToResolveUnavailabilityState();

        ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Integer> dummy = ArgumentCaptor.forClass(Integer.class);
        verify(mainThreadExecutor).executeDelayed(runnable.capture(), dummy.capture());
        when(playServicesHelper.getGooglePlayServicesState()).thenReturn(ServiceState.SERVICE_OK);
        runnable.getValue().run();

        PlayServicesAvailabilityFragmentViewState state = viewStateObservable.getValue();
        assert(state instanceof ReportPlayServicesAvailabilityState);
        assertTrue(((ReportPlayServicesAvailabilityState)state).googlePlayServicesIsAvailable);
    }

    @Test
    public void whenOnUserDoesNotWantToResolveUnavailabilityState_resultsInReportPlayServicesAvailabilityState() {
        viewModel.onUserDoesNotWantToResolveUnavailabilityState();

        PlayServicesAvailabilityFragmentViewState state = viewStateObservable.getValue();
        assertTrue(state instanceof ReportPlayServicesAvailabilityState);
        assertFalse(((ReportPlayServicesAvailabilityState)state).googlePlayServicesIsAvailable);
    }

    @Test
    public void whenOnGooglePlayServicesAvailableIsCalled_resultsInReportPlayServicesAvailabilityState() {
        viewModel.onGooglePlayServicesAvailable();

        PlayServicesAvailabilityFragmentViewState state = viewStateObservable.getValue();
        assertTrue(state instanceof ReportPlayServicesAvailabilityState);
        assertTrue(((ReportPlayServicesAvailabilityState)state).googlePlayServicesIsAvailable);
    }

    @Test
    public void whenOnGooglePlayServicesUnAvailableIsCalled_resultsInReportPlayServicesAvailabilityState() {
        viewModel.onGooglePlayServicesUnavailable();

        PlayServicesAvailabilityFragmentViewState state = viewStateObservable.getValue();
        assertTrue(state instanceof ReportPlayServicesAvailabilityState);
        assertFalse(((ReportPlayServicesAvailabilityState)state).googlePlayServicesIsAvailable);
    }
}