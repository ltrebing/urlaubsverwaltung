package org.synyx.urlaubsverwaltung.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ldap.core.LdapTemplate;
import org.synyx.urlaubsverwaltung.security.config.SecurityConfigurationProperties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LdapUserServiceImplTest {

    private LdapUserService sut;

    @Mock
    private LdapTemplate ldapTemplate;
    @Mock
    private LdapUserMapper ldapUserMapper;

    @Before
    public void setUp() {

        sut = new LdapUserServiceImpl(ldapTemplate, ldapUserMapper, new SecurityConfigurationProperties());
    }

    @Test
    public void ensureUsesLdapTemplateToFetchUsers() {

        sut.getLdapUsers();

        verify(ldapTemplate).search(any(), eq(ldapUserMapper));
    }
}
