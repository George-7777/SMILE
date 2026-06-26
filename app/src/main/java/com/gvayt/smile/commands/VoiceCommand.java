package com.gvayt.smile.commands;

/**
 * Базовый интерфейс для всех команд.
 */
public interface VoiceCommand {
    /**
     * @param voiceRequest запрос пользователя.
     * @return true если запрос соответствует команде, иначе false.
     */
    boolean matches(String voiceRequest);

    /**
     * Выполняет команду
     * @param voiceRequest запрос пользователя.
     */
    void execute(String voiceRequest);
}
