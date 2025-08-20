package smarttrade.backend.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import smarttrade.backend.TestDataUtil.itemTestData;
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.entities.userEntity;
import smarttrade.backend.service.itemService;
import smarttrade.backend.service.userService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class itemControllerIntegrationTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private itemService itemService;
    private userService userService;
    @Autowired
    public itemControllerIntegrationTest(MockMvc mockMvc, itemService itemService, userService userService) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.itemService=itemService;
        this.userService=userService;
    }
    @Test
    public void TestCreateItems() throws Exception {
        userEntity userEntity= userTestData.CreateUser();
        userEntity savedUser= userService.addUser(userEntity);
        itemEntity itemEntity= itemTestData.CreateItem1(savedUser);
        itemService.addItems(itemEntity);
        String item_jason= objectMapper.writeValueAsString(List.of(itemEntity));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/items/search")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].item_name").value("mouse"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].category").value("electronic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].user.name").value("hari mishra"));;

    }
    @Test
    public void TestGetAllItems() throws Exception {
        userEntity user = userTestData.CreateUser();
        userEntity savedUser=userService.addUser(user);
        itemEntity item1 = itemTestData.CreateItem1(savedUser);
        itemEntity item2 = itemTestData.CreateItem2(savedUser);
        itemService.addItems(item1);
        itemService.addItems(item2);

        mockMvc.perform(MockMvcRequestBuilders.get("/items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
    }

    @Test
    public void TestGetItemById() throws Exception {
        userEntity user = userTestData.CreateUser();
        user= userService.addUser(user);
        itemEntity item = itemTestData.CreateItem1(user);
        item = itemService.addItems(item);

        mockMvc.perform(MockMvcRequestBuilders.get("/items/{item_id}", item.getItem_id())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.item_name").value("mouse"));
    }

    @Test
    public void TestUpdateItem() throws Exception {
        userEntity user = userTestData.CreateUser();
        user=userService.addUser(user);
        itemEntity item = itemTestData.CreateItem1(user);
        item = itemService.addItems(item);
        item.setItem_name("keyboard");
        item.setCategory("electronic");

        String updateJson = objectMapper.writeValueAsString(item);

        mockMvc.perform(MockMvcRequestBuilders.patch("/items/{item_id}", item.getItem_id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.item_name").value("keyboard"));
    }

    @Test
    public void TestDeleteItem() throws Exception {
        userEntity user = userTestData.CreateUser();
        user=userService.addUser(user);
        itemEntity item = itemTestData.CreateItem1(user);
        item = itemService.addItems(item);

        mockMvc.perform(MockMvcRequestBuilders.delete("/items/{item_id}", item.getItem_id()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // Verify deleted by trying to fetch
        mockMvc.perform(MockMvcRequestBuilders.get("/items/{item_id}", item.getItem_id()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}


