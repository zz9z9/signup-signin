package com.study.login.service;

import com.study.login.domain.dto.MemberDto;
import com.study.login.repository.Member;
import com.study.login.repository.MemberRepository;
import com.study.login.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final Encryption encryption;
    private final JwtTokenProvider tokenProvider;

    @Override
    public String save(MemberDto member) {
        String pw = member.getPassword();
        String salt = encryption.getSalt();
        String encryptedPassword = encryption.getEncryptedPassword(pw, salt);
        ModelMapper modelMapper = new ModelMapper();
        Member memberEntity = modelMapper.map(member, Member.class);
        String[] roles = member.getId().equals("admin") ? new String[]{"ROLE_ADMIN", "ROLE_MANAGER"} : new String[]{"ROLE_GUEST", "ROLE_VIP"};

        memberEntity.setPassword(encryptedPassword);
        memberEntity.setSalt(salt);
        memberEntity.setRoles(Collections.arrayToList(roles));

        memberRepository.save(memberEntity);

        return memberEntity.getId();
    }

    @Override
    public Member find(String id) {
        Member findMember = memberRepository.findById(id).orElse(null);
        return findMember;
    }

    @Override
    public void sessionLogin(MemberDto member, HttpSession session) {
        String id = member.getId();
        String pw = member.getPassword();
        Member findMember = find(id);

        if (findMember != null) {
            if(isRightPassword(findMember, pw)) {
                System.out.println("비밀번호 일치!");

                session.setAttribute("loginId", id);
                session.setMaxInactiveInterval(60*10); // 세션 지속 시간
            } else {
                System.out.println("비밀번호 불일치!");
            }
        } else {
            System.out.println("일치하는 회원정보 없음 !");
        }
    }

    @Override
    public String tokenLogin(Map<String, Object> params) {
        String id = (String) params.get("id");
        String pw = (String) params.get("password");
        Member findMember = find(id);
        String jwtToken = null;

        if (findMember != null) {
            if(isRightPassword(findMember, pw)) {
                System.out.println("비밀번호 일치! 토큰을 생성합니다");
                jwtToken = tokenProvider.createToken(findMember.getUsername(), findMember.getRoles());
            } else {
                System.out.println("비밀번호 불일치!");
            }
        } else {
            System.out.println("일치하는 회원정보 없음 !");
        }

        return jwtToken;
    }

    private boolean isRightPassword(Member findMember, String passedPw) {
        String storedSalt = findMember.getSalt();
        String storedEncryptedPw = findMember.getPassword();
        String userPw = encryption.getEncryptedPassword(passedPw, storedSalt);

        return storedEncryptedPw.equals(userPw);
    }

}
