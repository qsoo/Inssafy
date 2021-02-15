package com.ssafy.pjt1.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.pjt1.model.dto.chat.ChatMessage;
import com.ssafy.pjt1.model.dto.user.UserDto;
import com.ssafy.pjt1.model.service.UserService;
import com.ssafy.pjt1.model.service.chat.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {
    private static final String SUCCESS = "success";
    private static final String FAIL = "fail";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate simpleMessageTemplate;

    @Autowired
    private UserService userService;

    private ObjectMapper objMapper;

    private ValueOperations<String, String> valOps;

    /*
     * 기능: 상대방과 중복된 방이 있는지 확인하고 없으면 방을 만든다
     * 
     * developer: 문진환
     * 
     * @param : user_id, user_id(상대방)
     * 
     * @return : room_id
     */
    @ApiOperation(value = "상대방과 중복된 방이 있는지 확인하고 없으면 방을 만든다")
    @PostMapping(value = "/checkRoomDupl")
    public ResponseEntity<Map<String, Object>> checkDupl(@RequestParam("my_id") String my_id,
            @RequestParam("opp_id") String opp_id) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.ACCEPTED;
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        log.info("my:{}, opp_id:{}", my_id, opp_id);
        try {
            // 중복체크
            String id1 = my_id + "/" + opp_id;
            String id2 = opp_id + "/" + my_id;
            if (setOps.isMember("check", id1) == false && setOps.isMember("check", id2) == false) {
                setOps.add("check", id1);
                setOps.add("check", id2);
                // 방 만들기
                String uid = chatService.makeRoom(my_id, opp_id);
                resultMap.put("roomId", uid);
                resultMap.put("message", SUCCESS);
            } else {// 있으면 못 만듬
                log.info("already exist");
                resultMap.put("message", FAIL);
            }
        } catch (Exception e) {
            log.info("error:{}", e);
            resultMap.put("message", FAIL);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<Map<String, Object>>(resultMap, status);
    }

    /*
     * 기능: 내 체팅방 리스트를 출력
     * 
     * developer: 문진환
     * 
     * @param : user_id, user_id(상대방)
     * 
     * @return : room_id
     */
    @ApiOperation(value = "내 채팅방 리스트를 출력")
    @PostMapping(value = "/getRoomList")
    public ResponseEntity<Map<String, Object>> getRoomList(@RequestParam("user_id") String user_id) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.ACCEPTED;
        objMapper = new ObjectMapper();
        try {
            List<Map<String, Object>> roomList = chatService.getRoomList(user_id);
            resultMap.put("roomInfo", roomList);
            resultMap.put("message", SUCCESS);
            status = HttpStatus.ACCEPTED;
        } catch (Exception e) {
            log.error("getRoomList: {}", e);
            resultMap.put("message", FAIL);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<Map<String, Object>>(resultMap, status);
    }

    /*
     * 기능: 상대방과 중복된 방이 있는지 확인하고 없으면 방을 만든다
     * 
     * developer: 문진환
     * 
     * @param : int startNUm, int endNum, string room_id
     * 
     * @return : message
     */
    @ApiOperation(value = "방에 입장 하고나서 메시지 리스트 출력")
    @PostMapping(value = "/enterRoom")
    public ResponseEntity<Map<String, Object>> readMessage(@RequestParam("startNUm") int startNUm,
            @RequestParam("endNUm") int endNum, @RequestParam("room_id") String room_id) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.ACCEPTED;
        // id 받고나서 초기화하기
        valOps = redisTemplate.opsForValue();
        // String key = "notice:" + user_id + ":" + opp_id;
        try {
            // valOps.set(key, "0");
            List<ChatMessage> list = chatService.getMessage(startNUm, endNum, room_id);
            log.info("메시지 출력");
            resultMap.put("msgList", list);
            resultMap.put("message", SUCCESS);
            status = HttpStatus.ACCEPTED;
        } catch (Exception e) {
            log.error("error: {}", e);
            resultMap.put("message", FAIL);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<Map<String, Object>>(resultMap, status);
    }

    /*
     * 기능: 클라이언트 쪽에서 보낸 메세지를 받음
     * 
     * developer: 문진환
     * 
     * @param :
     * 
     * @return :
     */
    @MessageMapping("/receive")
    public void greeting(ChatMessage message) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.ACCEPTED;
        String roomId = message.getRoom_id();
        String opp_id = message.getOpp_id();
        String user_id = message.getUser_id();
        valOps = redisTemplate.opsForValue();
        String key = "notice:" + user_id + ":" + opp_id;
        try {
            // valOps.increment(key, 1);
            // simpleMessageTemplate.convertAndSend("/notice/" + opp_id, message);
            chatService.insertMessage(message);
            status = HttpStatus.ACCEPTED;
            resultMap.put("message", SUCCESS);
            simpleMessageTemplate.convertAndSend("/message/" + roomId + "/" + opp_id, message);
        } catch (Exception e) {
            log.info("error:{}", e);
            resultMap.put("message", FAIL);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /*
     * 기능: notice 갖고오기
     * 
     * developer: 문진환
     * 
     * param: user_id(String)
     * 
     * return notices
     */
    @ApiOperation(value = "notice 갖고오기")
    @GetMapping(value = "/notice/{user_id}")
    public ResponseEntity<Map<String, Object>> getNotice(@PathVariable String user_id) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.ACCEPTED;
        String key = "notice:" + user_id + ":*";
        Set<String> keys = redisTemplate.keys(key);
        valOps = redisTemplate.opsForValue();
        List<Object> list = new LinkedList<>();
        try {
            // 나에게 보낸 사람들을 모두 조회
            for (String pattern : keys) {
                // {count : val, opp_name: "moon"} 이런 형태로 리스트에 저장
                Map<String, Object> res = new HashMap<>();
                String[] opp_ids = pattern.split(":");
                UserDto oppUserDto = userService.userDtoById(opp_ids[2]);
                res.put("count", valOps.get(pattern));
                res.put("opp_name", oppUserDto.getUser_nickname());
                list.add(res);
            }
            resultMap.put("message", SUCCESS);
            status = HttpStatus.ACCEPTED;
            resultMap.put("notices", list);
        } catch (Exception e) {
            resultMap.put("message", FAIL);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error("getNotice error: {}", e);
        }
        return new ResponseEntity<Map<String, Object>>(resultMap, status);
    }
}