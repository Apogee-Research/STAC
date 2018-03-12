package net.cybertip.note.helpers;

public class SubstituteLoggerServiceBuilder {
    private SubstituteLogger substituteLogger;

    public SubstituteLoggerServiceBuilder fixSubstituteLogger(SubstituteLogger substituteLogger) {
        this.substituteLogger = substituteLogger;
        return this;
    }

    public SubstituteLoggerService makeSubstituteLoggerService() {
        return new SubstituteLoggerService(substituteLogger);
    }
}