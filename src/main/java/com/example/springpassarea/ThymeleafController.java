package com.example.springpassarea;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
public class ThymeleafController {
    public static List<Integer> inputFiles = IntStream.range(1, 71).boxed().collect(Collectors.toList());

    @GetMapping({"/home", "/"})
    public ModelAndView home(@RequestParam(name = "case", required = false) Integer caseNum){
        long startTime = System.currentTimeMillis();
        if (caseNum == null) {
            caseNum = 1;
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("inputFiles", inputFiles);

        String path = "cases/" + caseNum + ".in";
        var inputs = InputReader.readFromFile(path);
        var lines = Solution.computePassArea(inputs);
        var svg = Draw.toSvg(inputs, lines);

        modelAndView.addObject("svg", svg);
        modelAndView.addObject("href", "home");

        modelAndView.setViewName("home");
        log.info("cost {}", Duration.ofMillis(System.currentTimeMillis() - startTime));
        return modelAndView;
    }


    @GetMapping("/planb")
    public ModelAndView planb(@RequestParam(name = "case", required = false) Integer caseNum){
        long startTime = System.currentTimeMillis();
        if (caseNum == null) {
            caseNum = 1;
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("inputFiles", inputFiles);

        String path = "cases/" + caseNum + ".in";
        var inputs = InputReader.readFromFile(path);
        var lines = SolutionPlanB.computePassArea(inputs);
        var svg = Draw.toSvg(inputs, lines);

        modelAndView.addObject("svg", svg);
        modelAndView.addObject("href", "planb");

        modelAndView.setViewName("home");
        log.info("cost {}", Duration.ofMillis(System.currentTimeMillis() - startTime));
        return modelAndView;
    }
}