package com.example.springpassarea;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.var;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ThymeleafController {
    public static List<Integer> inputFiles = IntStream.range(1, 41).boxed().collect(Collectors.toList());

    @GetMapping("/home")
    public ModelAndView home(@RequestParam(name = "case", required = false) Integer caseNum){
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

        modelAndView.setViewName("home");
        return modelAndView;
    }
}