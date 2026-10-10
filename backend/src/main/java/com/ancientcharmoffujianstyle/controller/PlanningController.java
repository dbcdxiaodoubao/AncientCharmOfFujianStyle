package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.controller.base.WebController;
import com.ancientcharmoffujianstyle.domain.query.PlanQuery;
import com.ancientcharmoffujianstyle.domain.vo.FyinfoListVo;
import com.ancientcharmoffujianstyle.domain.vo.PlanListVo;
import com.ancientcharmoffujianstyle.service.IFyinfoService;
import com.ancientcharmoffujianstyle.utils.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("plan")
@Api(tags = "路线规划")
public class PlanningController extends WebController {

    @Autowired
    private IFyinfoService fyinfoService;

    private static final int CITY_COUNT = 10;

    private static final int[][] CITY_ADJACENCY = buildAdjacency();

    private static int[][] buildAdjacency() {
        int[][] adj = new int[CITY_COUNT][CITY_COUNT];
        addEdge(adj, 1, 2); addEdge(adj, 1, 9);
        addEdge(adj, 2, 1); addEdge(adj, 2, 3);
        addEdge(adj, 3, 2); addEdge(adj, 3, 4); addEdge(adj, 3, 5);
        addEdge(adj, 3, 8); addEdge(adj, 3, 9);
        addEdge(adj, 4, 3); addEdge(adj, 4, 5);
        addEdge(adj, 5, 3); addEdge(adj, 5, 4); addEdge(adj, 5, 6);
        addEdge(adj, 5, 7);
        addEdge(adj, 6, 5); addEdge(adj, 6, 7);
        addEdge(adj, 7, 5); addEdge(adj, 7, 6); addEdge(adj, 7, 8);
        addEdge(adj, 8, 3); addEdge(adj, 8, 5); addEdge(adj, 8, 7);
        addEdge(adj, 8, 9);
        addEdge(adj, 9, 1); addEdge(adj, 9, 3); addEdge(adj, 9, 8);
        return adj;
    }

    private static void addEdge(int[][] adj, int from, int to) {
        adj[from][to] = 1;
        adj[to][from] = 1;
    }

    /**
     * BFS shortest path between two cities in Fujian province
     */
    private List<Integer> findShortestPath(int start, int end) {
        if (start == end) {
            return Collections.singletonList(start);
        }

        int[] prev = new int[CITY_COUNT];
        boolean[] visited = new boolean[CITY_COUNT];
        Arrays.fill(prev, -1);

        Deque<Integer> queue = new ArrayDeque<>();
        queue.offer(start);
        visited[start] = true;

        while (!queue.isEmpty()) {
            int current = queue.poll();
            for (int neighbor = 1; neighbor < CITY_COUNT; neighbor++) {
                if (CITY_ADJACENCY[current][neighbor] == 1 && !visited[neighbor]) {
                    visited[neighbor] = true;
                    prev[neighbor] = current;
                    queue.offer(neighbor);
                    if (neighbor == end) {
                        return reconstructPath(prev, start, end);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private List<Integer> reconstructPath(int[] prev, int start, int end) {
        LinkedList<Integer> path = new LinkedList<>();
        for (int at = end; at != -1; at = prev[at]) {
            path.addFirst(at);
        }
        return path;
    }

    @PostMapping
    @ApiOperation("规划")
    public ApiResponse<List<PlanListVo>> plan(@RequestBody @Validated PlanQuery planQuery) {
        int start = planQuery.getStart().intValue();
        int end = planQuery.getEnd().intValue();

        List<Integer> cityPath = findShortestPath(start, end);
        if (cityPath.isEmpty()) return ApiResponse.error("出发和结束城市之间暂无可用路线", null);

        List<PlanListVo> result = new ArrayList<>();
        long order = 1L;

        for (Integer cityId : cityPath) {
            List<FyinfoListVo> cityItems = fyinfoService.listByCity((long) cityId);
            if (cityItems == null) continue;
            int count = 0;
            for (FyinfoListVo item : cityItems) {
                if (count >= 2) break;
                PlanListVo vo = new PlanListVo();
                vo.setId(item.getId());
                vo.setName(item.getName());
                vo.setCity(item.getCity());
                vo.setPictureUrl(item.getPictureUrl());
                vo.setType(item.getType());
                vo.setLevel(item.getLevel());
                vo.setOrder(order++);
                result.add(vo);
                count++;
            }
        }

        if (result.isEmpty()) {
            return ApiResponse.error("未找到该路线上的非遗项目", null);
        }

        return ApiResponse.success(start == end ? "已生成同城非遗路线" : "路线规划成功", result);
    }
}
