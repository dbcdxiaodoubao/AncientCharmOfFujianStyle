package com.ancientcharmoffujianstyle.service;

import com.ancientcharmoffujianstyle.domain.entity.UserPreference;

public interface IUserPreferenceService {

    UserPreference getPreference(Long userId);

    void savePreference(UserPreference preference);
}
